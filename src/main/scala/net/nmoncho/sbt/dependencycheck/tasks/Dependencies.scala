/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckCompat
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
        val converter          = fileConverter.value

        if (scopes.compile) {
          dependencies ++= logAddDependencies(
            DependencyCheckCompat.managedJars(Compile, classpathTypeValue, updateValue, converter),
            Compile,
            converter
          )
        }

        if (scopes.test) {
          dependencies ++= logAddDependencies(
            DependencyCheckCompat.managedJars(Test, classpathTypeValue, updateValue, converter),
            Test,
            converter
          )
        }

        // Provided dependencies are include in Compile dependencies: remove instead of adding
        if (scopes.provided) {
          dependencies ++= logAddDependencies(
            DependencyCheckCompat.managedJars(Provided, classpathTypeValue, updateValue, converter),
            Provided,
            converter
          )
        }

        if (scopes.runtime) {
          dependencies ++= logAddDependencies(
            DependencyCheckCompat.managedJars(Runtime, classpathTypeValue, updateValue, converter),
            Runtime,
            converter
          )
        }

        // Optional dependencies are include in Compile dependencies: remove instead of adding
        if (scopes.optional) {
          dependencies ++= logAddDependencies(
            DependencyCheckCompat.managedJars(Optional, classpathTypeValue, updateValue, converter),
            Optional,
            converter
          )
        }

        dependencies.toSet
      }
    }
  }

}
