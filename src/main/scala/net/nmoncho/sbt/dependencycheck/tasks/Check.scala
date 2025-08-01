/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package tasks

import scala.jdk.CollectionConverters._

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.scanSet
import net.nmoncho.sbt.dependencycheck.Keys._
import net.nmoncho.sbt.dependencycheck.settings.ScopesSettings
import net.nmoncho.sbt.dependencycheck.settings.SummaryReport
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import org.owasp.dependencycheck.analyzer.AbstractSuppressionAnalyzer.SUPPRESSION_OBJECT_KEY
import org.owasp.dependencycheck.reporting.ReportGenerator
import org.owasp.dependencycheck.xml.suppression.{ SuppressionRule => DcSuppressionRule }
import sbt.Keys._
import sbt._
import sbt.complete.Parser
import sbt.plugins.JvmPlugin

object Check {

  private[tasks] val argumentsParser: Parser[Seq[ParseOptions]] =
    (ListSettingsArg | SingleReportArg | AllProjectsArg | ListUnusedSuppressionsArg | OriginalSummaryArg | AllVulnerabilitiesSummaryArg | OffendingVulnerabilitiesSummaryArg).*

  private case class CheckSettings(
      name: String,
      scopes: ScopesSettings,
      failureScore: Double,
      scanSet: Seq[File],
      engineSettings: org.owasp.dependencycheck.utils.Settings,
      dependencies: Set[Attributed[File]],
      suppressions: Set[SuppressionRule],
      outputDirectory: File,
      reportFormats: Seq[ReportGenerator.Format]
  )

  def apply(): Def.Initialize[InputTask[Unit]] = Def.inputTaskDyn {
    implicit val log: Logger = streams.value.log

    val arguments              = argumentsParser.parsed
    val singleReport           = arguments.contains(ParseOptions.SingleReport)
    val allProjects            = arguments.contains(ParseOptions.AllProjects)
    val listUnusedSuppressions = arguments.contains(ParseOptions.ListUnusedSuppressions)

    val summary = arguments.find(arg =>
      arg == ParseOptions.OriginalSummary || arg == ParseOptions.AllVulnerabilitiesSummary || arg == ParseOptions.OffendingVulnerabilitiesSummary
    ) match {
      case Some(ParseOptions.AllVulnerabilitiesSummary) => SummaryReport.AllVulnerabilities
      case Some(ParseOptions.OffendingVulnerabilitiesSummary) =>
        SummaryReport.OffendingVulnerabilities
      case _ => SummaryReport.Original
    }

    val dependenciesAndSuppressionsTask = Def.taskDyn {
      if (singleReport && allProjects) {
        allProjectsSettings.map(Seq(_))
      } else if (singleReport) {
        aggregateProjectsSettings.map(Seq(_))
      } else if (!singleReport) {
        aggregateProjectsFilter.map(_.flatten)
      } else {
        sys.error("'all-projects' argument isn't supported without the use of 'single-project'")
      }
    }

    // Don't run if this project has been configured to be skipped
    // But if it's a singleReport, then users may run the `dependencyCheckAggregate`
    if (!dependencyCheckSkip.value || singleReport) {
      Def.task {
        dependenciesAndSuppressionsTask.value.foreach { checkSettings =>
          log.info(s"Running dependency check for [${checkSettings.name}]")

          val settings = checkSettings.engineSettings

          log.info("Scanning following dependencies: ")
          checkSettings.dependencies.foreach(f => log.info("\t" + f.data.getName))

          if (arguments.contains(ParseOptions.ListSettings)) {
            log.info(s"\nDependencyCheck settings for [${checkSettings.name}]:")
            ListSettings(settings, checkSettings.scopes)
          }

          withEngine(settings) { engine =>
            val failureOpt =
              try {
                analyzeProject(
                  checkSettings.name,
                  engine,
                  checkSettings.dependencies,
                  checkSettings.suppressions,
                  checkSettings.scanSet,
                  checkSettings.failureScore,
                  checkSettings.outputDirectory,
                  checkSettings.reportFormats,
                  summary
                )

                Option.empty[Throwable] // success
              } catch {
                case t: VulnerabilityFoundException if listUnusedSuppressions =>
                  // Hold and throw later
                  Some(t)
              }

            if (listUnusedSuppressions) {
              val unusedSuppressions = engine
                .getObject(SUPPRESSION_OBJECT_KEY)
                .asInstanceOf[java.util.List[DcSuppressionRule]]
                .asScala
                .filter(sup => !sup.isMatched && !sup.isBase)

              if (unusedSuppressions.nonEmpty) {
                log.info(s"""
                            |
                            |Found [${unusedSuppressions.size}] unused suppressions for project [${checkSettings.name}]:
                            |${unusedSuppressions.mkString("\n\t", "\n\t", "\n")}
                            |
                            |""".stripMargin)
              } else {
                log.info("No unused suppressions.")
              }

              // We must throw an exception if it was caught to list the unused suppressions
              failureOpt.foreach(ex => throw ex)
            }
          }
        }
      } tag NonParallel
    } else {
      Def.task {
        log.info(s"Skipping dependency check for [${name.value}]")
      }
    }
  } tag NonParallel

  private lazy val allProjectsSettings: Def.Initialize[Task[CheckSettings]] = Def.task {
    CheckSettings(
      name.value,
      dependencyCheckScopes.value,
      dependencyCheckFailBuildOnCVSS.value,
      scanSet.value,
      engineSettings.value,
      AllProjectsCheck.dependencies().value,
      AllProjectsCheck.suppressions().value,
      dependencyCheckOutputDirectory.value,
      dependencyCheckFormats.value
    )
  }

  private lazy val aggregateProjectsSettings: Def.Initialize[Task[CheckSettings]] = Def.task {
    CheckSettings(
      name.value,
      dependencyCheckScopes.value,
      dependencyCheckFailBuildOnCVSS.value,
      scanSet.value,
      engineSettings.value,
      AggregateCheck.dependencies().value,
      AggregateCheck.suppressions().value,
      dependencyCheckOutputDirectory.value,
      dependencyCheckFormats.value
    )
  }

  private lazy val aggregateProjectsFilter = Def.settingDyn {
    perProjectSettingsTask.all(ScopeFilter(inAggregates(thisProjectRef.value)))
  }

  private lazy val perProjectSettingsTask: Def.Initialize[Task[Seq[CheckSettings]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) || (dependencyCheckSkip ?? false).value
      )
        Def.task(Seq.empty[CheckSettings])
      else
        Def.task(
          Seq(
            CheckSettings(
              name.value,
              dependencyCheckScopes.value,
              dependencyCheckFailBuildOnCVSS.value,
              scanSet.value,
              engineSettings.value,
              Dependencies.projectDependencies.value,
              GenerateSuppressions.forProject.value,
              dependencyCheckOutputDirectory.value,
              dependencyCheckFormats.value
            )
          )
        )
    }
}
