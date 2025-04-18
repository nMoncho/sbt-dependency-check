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

object AggregateCheck {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log
    log.info(s"Running aggregate check for [${name.value}]")

    val failCvssScore         = dependencyCheckFailBuildOnCVSS.value
    val aggregateDependencies = dependencies().value
    val suppressionRules      = suppressions().value
    val scanSetFiles          = scanSet.value

    log.info("Scanning following dependencies: ")

    withEngine(engineSettings.value) { engine =>
      analyzeProject(
        name.value,
        engine,
        aggregateDependencies,
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
    dependencies ++= logAddDependencies(aggregateCompileFilter.value.flatten, Compile)
    dependencies ++= logAddDependencies(aggregateTestFilter.value.flatten, Test)
    dependencies ++= logAddDependencies(aggregateRuntimeFilter.value.flatten, Runtime)
    dependencies --= logRemoveDependencies(aggregateProvidedFilter.value.flatten, Provided)
    dependencies --= logRemoveDependencies(aggregateOptionalFilter.value.flatten, Optional)

    dependencies.toSet
  }

  def suppressions(): Def.Initialize[Task[Set[SuppressionRule]]] = Def.task {
    suppressionRulesFilter.value.flatten.toSet
  }

  private lazy val aggregateCompileFilter = Def.settingDyn {
    compileDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Compile))
    )
  }

  private lazy val aggregateRuntimeFilter = Def.settingDyn {
    runtimeDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Runtime))
    )
  }

  private lazy val aggregateTestFilter = Def.settingDyn {
    testDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Test))
    )
  }

  private lazy val aggregateProvidedFilter = Def.settingDyn {
    providedDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Provided))
    )
  }

  private lazy val aggregateOptionalFilter = Def.settingDyn {
    optionalDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Optional))
    )
  }

  private lazy val suppressionRulesFilter = Def.settingDyn {
    suppressionRulesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Compile))
    )
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
          val dependencies         = AggregateCheck.dependencies().value

          val buildSuppressions = settings.suppressions
          val importedPackagedSuppressions = collectImportedPackagedSuppressions(
            settings,
            dependencies
          )

          buildSuppressions ++ importedPackagedSuppressions
        }
    }
}
