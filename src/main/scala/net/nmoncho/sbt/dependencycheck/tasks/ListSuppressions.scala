/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckSkip
import sbt.Def
import sbt.InputTask
import sbt.Keys.name
import sbt.Keys.streams
import sbt.Logger
import sbt.complete.DefaultParsers._
import sbt.complete.Parser

/** Lists Suppression Rules that are added to the Owasp Engine by defining them on
  * the project definition (ie. `build.sbt`) or imported as packaged suppressions rules.
  *
  * The goal of this task is to make visible to users what suppressions are being added
  * by the SBT plugin but not through the usual DependencyCheck configuration (e.g. properties file)
  */
object ListSuppressions {

  private val PerProject  = (Space ~> token("per-project")) ^^^ ParseResult.PerProject
  private val AllProjects = (Space ~> token("all-projects")) ^^^ ParseResult.AllProjects
  private val Aggregate   = (Space ~> token("aggregate")) ^^^ ParseResult.Aggregate

  private val listParser: Parser[Option[ParseResult]] = (PerProject | AllProjects | Aggregate).?

  def apply(): Def.Initialize[InputTask[Unit]] = Def.inputTaskDyn {
    implicit val log: Logger = streams.value.log

    if (!dependencyCheckSkip.value) {
      Def.task {
        val rules = (listParser.parsed match {
          case Some(ParseResult.AllProjects) => GenerateSuppressions.forAllProjects()
          case Some(ParseResult.Aggregate) => GenerateSuppressions.forAggregate()
          case Some(ParseResult.PerProject) | _ => GenerateSuppressions.forProject()
        }).value

        if (rules.nonEmpty) {
          log.info(s"Suppression rules added for [${name.value}]")
          rules.foreach(rule => log.info(s"\t${rule.toOwasp.toString}"))
          log.info("\n\n")
        } else {
          log.info(s"No suppression rules added for [${name.value}]")
          log.info("\n\n")
        }
      } tag NonParallel
    } else {
      Def.task {
        log.info(s"Skipping dependency check for [${name.value}]")
      }
    }
  } tag NonParallel

  private sealed abstract class ParseResult extends Product with Serializable
  private object ParseResult {
    case object PerProject extends ParseResult
    case object AllProjects extends ParseResult
    case object Aggregate extends ParseResult
  }

}
