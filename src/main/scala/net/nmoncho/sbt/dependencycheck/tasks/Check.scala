package net.nmoncho.sbt.dependencycheck
package tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.{ engineSettings, scanSet }
import net.nmoncho.sbt.dependencycheck.Keys.*
import sbt.*
import sbt.Keys.*

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
