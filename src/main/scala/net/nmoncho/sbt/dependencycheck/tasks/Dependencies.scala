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

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckScopes
import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckSkip
import net.nmoncho.sbt.dependencycheck.settings.ScopesSettings
import sbt.Keys._
import sbt._
import sbt.internal.util.Attributed
import sbt.plugins.JvmPlugin

object Dependencies {

  def projectDependencies: Def.Initialize[Task[Set[Attributed[File]]]] = Def.task {
    implicit val log: Logger = streams.value.log

    val dependencies        = scala.collection.mutable.Set[Attributed[File]]()
    val scopes              = dependencyCheckScopes.value
    val compileDependencies = (Compile / externalDependencyClasspath).value
    val testDependencies    = (Test / externalDependencyClasspath).value
    val runtimeDependencies = (Runtime / externalDependencyClasspath).value

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

    dependencies.toSet
  }

  lazy val compileDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) ||
        (dependencyCheckSkip ?? false).value ||
        !(dependencyCheckScopes ?? ScopesSettings.Default).value.compile
      )
        Def.task(Seq.empty)
      else
        Def.task {
          (configuration / externalDependencyClasspath).value
        }
    }

  lazy val runtimeDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) ||
        (dependencyCheckSkip ?? false).value ||
        !(dependencyCheckScopes ?? ScopesSettings.Default).value.runtime
      )
        Def.task(Seq.empty)
      else
        Def.task {
          (configuration / externalDependencyClasspath).value
        }
    }

  lazy val testDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) ||
        (dependencyCheckSkip ?? false).value ||
        !(dependencyCheckScopes ?? ScopesSettings.Default).value.test
      )
        Def.task(Seq.empty)
      else
        Def.task {
          (configuration / externalDependencyClasspath).value
        }
    }

  lazy val providedDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) ||
        (dependencyCheckSkip ?? false).value ||
        (dependencyCheckScopes ?? ScopesSettings.Default).value.provided
      )
        Def.task(Seq.empty)
      else
        Def.task {
          Classpaths.managedJars(configuration.value, classpathTypes.value, update.value)
        }
    }

  lazy val optionalDependenciesTask: Def.Initialize[Task[Seq[Attributed[File]]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) ||
        (dependencyCheckSkip ?? false).value ||
        (dependencyCheckScopes ?? ScopesSettings.Default).value.optional
      )
        Def.task(Seq.empty)
      else
        Def.task {
          Classpaths.managedJars(configuration.value, classpathTypes.value, update.value)
        }
    }
}
