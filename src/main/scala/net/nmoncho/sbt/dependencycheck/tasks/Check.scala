/*
 * Copyright (c) 2025 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

        val failCvssScore       = dependencyCheckFailBuildOnCVSS.value
        val dependencies        = scala.collection.mutable.Set[Attributed[File]]()
        val scopes              = dependencyCheckScopes.value
        val compileDependencies = (Compile / externalDependencyClasspath).value
        val testDependencies    = (Test / externalDependencyClasspath).value
        val runtimeDependencies = (Runtime / externalDependencyClasspath).value
        val scanSetFiles        = scanSet.value

        val classpathTypeValue = classpathTypes.value
        val updateValue        = update.value

        // Dependencies are collected in a specific order based on scope.
        // We follow the same order as `net.vonbuchholtz`
        if (scopes.compile) {
          dependencies ++= logAddDependencies(compileDependencies, Compile)
        }

        // Provided dependencies are include in Compile dependencies: remove instead of adding
        if (!scopes.provided) {
          dependencies --= logRemoveDependencies(
            Classpaths.managedJars(Provided, classpathTypeValue, updateValue),
            Provided
          )
        }

        if (scopes.runtime) {
          dependencies ++= logAddDependencies(runtimeDependencies, Runtime)
        }

        if (scopes.test) {
          dependencies ++= logAddDependencies(testDependencies, Test)
        }

        // Optional dependencies are include in Compile dependencies: remove instead of adding
        if (!scopes.optional) {
          dependencies --= logRemoveDependencies(
            Classpaths.managedJars(Optional, classpathTypeValue, updateValue),
            Optional
          )
        }

        log.info("Scanning following dependencies: ")
        dependencies.foreach(f => log.info("\t" + f.data.getName))

        withEngine(engineSettings.value) { engine =>
          analyzeProject(
            name.value,
            engine,
            dependencies.toSet,
            scanSetFiles,
            failCvssScore,
            dependencyCheckOutputDirectory.value,
            dependencyCheckFormats.value
          )
        }
      }
    } else {
      Def.task {
        log.info(s"Skipping dependency check for [${name.value}]")
      }
    }
  }

}
