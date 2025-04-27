/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.scanSet
import net.nmoncho.sbt.dependencycheck.Keys._
import sbt.Def
import sbt.Keys._
import sbt._
import sbt.complete.Parser

object Check {

  private[tasks] val listSettingsParser: Parser[Seq[ParseOptions]] =
    (ListSettingsArg | SingleReportArg).*

  def apply(): Def.Initialize[InputTask[Unit]] = Def.inputTaskDyn {
    implicit val log: Logger = streams.value.log

    val arguments    = listSettingsParser.parsed
    val singleReport = arguments.contains(ParseOptions.SingleReport)

    // Don't run if this project has been configured to be skipped
    // But if it's a singleReport, then users may run the `dependencyCheckAggregate`
    if (!dependencyCheckSkip.value || singleReport) {
      Def.taskDyn {
        log.info(s"Running check for [${name.value}]")

        val (dependencies, suppressionRules) = if (singleReport) {
          AggregateCheck.dependencies().value -> AggregateCheck.suppressions().value
        } else {
          Dependencies.projectDependencies.value -> GenerateSuppressions.forProject().value
        }

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
