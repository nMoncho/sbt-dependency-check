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
import sbt.Keys._
import sbt._

object Check {

  def apply(): Def.Initialize[Task[Unit]] = Def.taskDyn {
    implicit val log: Logger = streams.value.log

    if (!dependencyCheckSkip.value) {
      Def.task {
        log.info(s"Running check for [${name.value}]")

        val failCvssScore    = dependencyCheckFailBuildOnCVSS.value
        val dependencies     = Dependencies.projectDependencies.value
        val suppressionRules = GenerateSuppressions.forProject().value
        val scanSetFiles     = scanSet.value

        log.info("Scanning following dependencies: ")
        dependencies.foreach(f => log.info("\t" + f.data.getName))

        withEngine(engineSettings.value) { engine =>
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
      } tag NonParallel
    } else {
      Def.task {
        log.info(s"Skipping dependency check for [${name.value}]")
      }
    }
  } tag NonParallel

}
