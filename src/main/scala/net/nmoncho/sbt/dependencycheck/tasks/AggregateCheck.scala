/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Keys._
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import net.nmoncho.sbt.dependencycheck.tasks.GenerateSuppressions.collectImportedPackagedSuppressions
import sbt.Def
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object AggregateCheck {

  def apply(): Def.Initialize[Task[Unit]] = Check().toTask(" single-report")

  def dependencies(): Def.Initialize[Task[Set[Attributed[File]]]] = Def.task {
    dependenciesFilter.value.toSet.flatten
  }

  def suppressions(): Def.Initialize[Task[Set[SuppressionRule]]] = Def.task {
    suppressionRulesFilter.value.flatten.toSet
  }

  private lazy val dependenciesFilter = Def.settingDyn {
    Dependencies.projectDependencies.all(
      ScopeFilter(inAggregates(thisProjectRef.value))
    )
  }

  private lazy val suppressionRulesFilter = Def.settingDyn {
    suppressionRulesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value))
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
