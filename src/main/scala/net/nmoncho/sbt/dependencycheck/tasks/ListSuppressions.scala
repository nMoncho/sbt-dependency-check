/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Keys._
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import net.nmoncho.sbt.dependencycheck.tasks.GenerateSuppressions.collectImportedPackagedSuppressions
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

/** Lists Suppression Rules that are added to the Owasp Engine by defining them on
  * the project definition (ie. `build.sbt`) or imported as packaged suppressions rules.
  *
  * The goal of this task is to make visible to users what suppressions are being added
  * by the SBT plugin but not through the usual DependencyCheck configuration (e.g. properties file)
  */
object ListSuppressions {

  def apply(): Def.Initialize[InputTask[Unit]] = Def.inputTaskDyn {
    implicit val log: Logger = streams.value.log

    Def.task {
      val rules = listParser.parsed match {
        case Some(ParseResult.AllProjects) =>
          Seq(name.value -> AllProjectsCheck.suppressions().tag(NonParallel).value)

        case Some(ParseResult.Aggregate) =>
          Seq(name.value -> AggregateCheck.suppressions().tag(NonParallel).value)

        case Some(ParseResult.PerProject) | _ =>
          suppressionRulesFilter.tag(NonParallel).value.sortBy { case (name, _) => name }
      }

      rules.foreach { case (name, rules) =>
        if (rules.nonEmpty) {
          log.info(s"Suppression rules added for [$name]")
          rules.foreach(rule => log.info(s"\t${rule.toOwasp.toString}"))
          log.info("\n\n")
        } else {
          log.info(s"No suppression rules added for [$name]")
          log.info("\n\n")
        }
      }
    } tag NonParallel
  }

  private lazy val suppressionRulesFilter = Def.settingDyn {
    suppressionRulesTask
      .all(ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Compile)))
  }

  private lazy val suppressionRulesTask: Def.Initialize[Task[(String, Set[SuppressionRule])]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) || (dependencyCheckSkip ?? false).value
      )
        Def.task(name.value -> Set.empty)
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

          name.value -> (buildSuppressions ++ importedPackagedSuppressions).toSet
        }
    }
}
