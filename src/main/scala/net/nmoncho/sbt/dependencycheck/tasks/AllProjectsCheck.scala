/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import sbt._

object AllProjectsCheck {

  def apply(): Def.Initialize[Task[Unit]] = Check().toTask(" single-report all-projects")

  def dependencies(): Def.Initialize[Task[Set[Attributed[File]]]] = Def.task {
    dependenciesFilter.value.toSet.flatten
  }

  def suppressions(): Def.Initialize[Task[Set[SuppressionRule]]] = Def.task {
    suppressionRulesFilter.value.flatten.toSet
  }

  private lazy val dependenciesFilter = Def.settingDyn {
    Dependencies.projectDependencies.all(
      ScopeFilter(inAnyProject)
    )
  }

  private lazy val suppressionRulesFilter = Def.settingDyn {
    GenerateSuppressions.forProject.all(ScopeFilter(inAnyProject))
  }
}
