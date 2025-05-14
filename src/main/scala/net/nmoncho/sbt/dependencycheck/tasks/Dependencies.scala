/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckScopes
import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckSkip
import sbt.Keys._
import sbt._
import sbt.internal.util.Attributed
import sbt.plugins.JvmPlugin

object Dependencies {

  lazy val projectDependencies: Def.Initialize[Task[Set[Attributed[File]]]] = Def.taskDyn {
    if (
      !thisProject.value.autoPlugins.contains(JvmPlugin) || (dependencyCheckSkip ?? false).value
    ) {
      Def.task(Set.empty)
    } else {
      Def.task {
        implicit val log: Logger = streams.value.log

        val dependencies       = scala.collection.mutable.Set[Attributed[File]]()
        val scopes             = dependencyCheckScopes.value
        val classpathTypeValue = classpathTypes.value
        val updateValue        = update.value

        if (scopes.compile) {
          dependencies ++= logAddDependencies(
            Classpaths.managedJars(Compile, classpathTypeValue, updateValue),
            Compile
          )
        }

        if (scopes.test) {
          dependencies ++= logAddDependencies(
            Classpaths.managedJars(Test, classpathTypeValue, updateValue),
            Test
          )
        }

        // Provided dependencies are include in Compile dependencies: remove instead of adding
        if (scopes.provided) {
          dependencies ++= logAddDependencies(
            Classpaths.managedJars(Provided, classpathTypeValue, updateValue),
            Provided
          )
        }

        if (scopes.runtime) {
          dependencies ++= logAddDependencies(
            Classpaths.managedJars(Runtime, classpathTypeValue, updateValue),
            Runtime
          )
        }

        // Optional dependencies are include in Compile dependencies: remove instead of adding
        if (scopes.optional) {
          dependencies ++= logAddDependencies(
            Classpaths.managedJars(Optional, classpathTypeValue, updateValue),
            Optional
          )
        }

        dependencies.toSet
      }
    }
  }

}
