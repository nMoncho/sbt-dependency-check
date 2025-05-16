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
import org.owasp.dependencycheck.analyzer.AbstractSuppressionAnalyzer.SUPPRESSION_OBJECT_KEY
import org.owasp.dependencycheck.xml.suppression.SuppressionRule
import sbt.Keys._
import sbt._
import sbt.complete.Parser

object Check {

  private[tasks] val argumentsParser: Parser[Seq[ParseOptions]] =
    (ListSettingsArg | SingleReportArg | AllProjectsArg | ListUnusedSuppressionsArg).*

  def apply(): Def.Initialize[InputTask[Unit]] = Def.inputTaskDyn {
    implicit val log: Logger = streams.value.log

    val arguments              = argumentsParser.parsed
    val singleReport           = arguments.contains(ParseOptions.SingleReport)
    val allProjects            = arguments.contains(ParseOptions.AllProjects)
    val listUnusedSuppressions = arguments.contains(ParseOptions.ListUnusedSuppressions)

    val dependenciesAndSuppressionsTask = Def.taskDyn {
      if (singleReport && allProjects) {
        log.info(s"Running AllProjects dependency check for [${name.value}]")
        Def.task(AllProjectsCheck.dependencies().value -> AllProjectsCheck.suppressions().value)
      } else if (singleReport) {
        log.info(s"Running Aggregate dependency check for [${name.value}]")
        Def.task(AggregateCheck.dependencies().value -> AggregateCheck.suppressions().value)
      } else if (!singleReport) {
        log.info(s"Running dependency check for [${name.value}]")
        Def.task(Dependencies.projectDependencies.value -> GenerateSuppressions.forProject.value)
      } else {
        sys.error("'all-projects' argument isn't supported without the use of 'single-project'")
      }
    }

    // Don't run if this project has been configured to be skipped
    // But if it's a singleReport, then users may run the `dependencyCheckAggregate`
    if (!dependencyCheckSkip.value || singleReport) {
      Def.taskDyn {
        val (dependencies, suppressionRules) = dependenciesAndSuppressionsTask.value

        val failCvssScore = dependencyCheckFailBuildOnCVSS.value
        val scanSetFiles  = scanSet.value
        val settings      = engineSettings.value

        log.info("Scanning following dependencies: ")
        dependencies.foreach(f => log.info("\t" + f.data.getName))

        if (arguments.contains(ParseOptions.ListSettings)) {
          log.info(s"\nDependencyCheck settings for [${name.value}]:")
          ListSettings(settings, dependencyCheckScopes.value)
        }

        Def.task(
          withEngine(settings) { engine =>
            try {
              analyzeProject(
                name.value,
                engine,
                dependencies,
                suppressionRules,
                scanSetFiles,
                failCvssScore,
                dependencyCheckOutputDirectory.value,
                dependencyCheckFormats.value
              )
            } catch {
              case _: VulnerabilityFoundException if listUnusedSuppressions => // ignore
            }

            if (listUnusedSuppressions) {
              val unusedSuppressions = engine
                .getObject(SUPPRESSION_OBJECT_KEY)
                .asInstanceOf[java.util.List[SuppressionRule]]
                .asScala
                .filter(sup => !sup.isMatched && !sup.isBase)

              if (unusedSuppressions.nonEmpty) {
                log.info(s"""
                            |
                            |Found [${unusedSuppressions.size}] unused suppressions for project [${name.value}]:
                            |${unusedSuppressions.mkString("\n\t", "\n\t", "\n")}
                            |
                            |""".stripMargin)
              } else {
                log.info("No unused suppressions.")
              }
            }
          }
        )
      } tag NonParallel
    } else {
      Def.task {
        log.info(s"Skipping dependency check for [${name.value}]")
      }
    }
  } tag NonParallel

}
