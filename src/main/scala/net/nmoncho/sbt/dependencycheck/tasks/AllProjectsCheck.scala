/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.scanSet
import net.nmoncho.sbt.dependencycheck.Keys._
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import net.nmoncho.sbt.dependencycheck.tasks.Dependencies._
import net.nmoncho.sbt.dependencycheck.tasks.GenerateSuppressions.collectImportedPackagedSuppressions
import sbt.Def
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object AllProjectsCheck {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log
    log.info(s"Running AllProjects check for [${name.value}]")

    val failCvssScore    = dependencyCheckFailBuildOnCVSS.value
    val allDependencies  = dependencies().value
    val suppressionRules = suppressions().value
    val scanSetFiles     = scanSet.value

    log.info("Scanning following dependencies: ")
    allDependencies.foreach(f => log.info("\t" + f.data.getName))

    withEngine(engineSettings.value) { engine =>
      analyzeProject(
        name.value,
        engine,
        allDependencies,
        suppressionRules,
        scanSetFiles,
        failCvssScore,
        dependencyCheckOutputDirectory.value,
        dependencyCheckFormats.value
      )
    }
  }

  def dependencies(): Def.Initialize[Task[Set[Attributed[File]]]] = Def.task {
    implicit val log: Logger = streams.value.log

    val dependencies = scala.collection.mutable.Set[Attributed[File]]()
    dependencies ++= logAddDependencies(anyCompileFilter.value.flatten, Compile)
    dependencies --= logRemoveDependencies(anyProvidedFilter.value.flatten, Provided)
    dependencies ++= logAddDependencies(anyRuntimeFilter.value.flatten, Runtime)
    dependencies ++= logAddDependencies(anyTestFilter.value.flatten, Test)
    dependencies --= logRemoveDependencies(anyOptionalFilter.value.flatten, Optional)

    dependencies.toSet
  }

  def suppressions(): Def.Initialize[Task[Set[SuppressionRule]]] = Def.task {
    suppressionRulesFilter.value.flatten.toSet
  }

  private lazy val anyCompileFilter = Def.settingDyn {
    compileDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Compile)))
  }
  private lazy val anyRuntimeFilter = Def.settingDyn {
    runtimeDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Runtime)))
  }
  private lazy val anyTestFilter = Def.settingDyn {
    testDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Test)))
  }
  private lazy val anyProvidedFilter = Def.settingDyn {
    providedDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Provided)))
  }
  private lazy val anyOptionalFilter = Def.settingDyn {
    optionalDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Optional)))
  }
  private lazy val suppressionRulesFilter = Def.settingDyn {
    suppressionRulesTask.all(ScopeFilter(inAnyProject, inConfigurations(Compile)))
  }

  private lazy val suppressionRulesTask: Def.Initialize[Task[Seq[SuppressionRule]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) || (dependencyCheckSkip ?? false).value
      )
        Def.task(Seq.empty)
      else
        Def.task {
          implicit val log: Logger = streams.value.log
          val settings             = dependencyCheckSuppressions.value
          val dependencies         = AllProjectsCheck.dependencies().value

          val buildSuppressions = settings.suppressions
          val importedPackagedSuppressions = collectImportedPackagedSuppressions(
            settings,
            dependencies
          )

          buildSuppressions ++ importedPackagedSuppressions
        }
    }
}
