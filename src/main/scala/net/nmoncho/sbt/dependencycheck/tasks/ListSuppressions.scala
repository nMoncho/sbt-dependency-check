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

  private val listParser: Parser[ParseResult] = PerProject | AllProjects | Aggregate

  def apply(): Def.Initialize[InputTask[Unit]] = Def.inputTaskDyn {
    implicit val log: Logger = streams.value.log

    if (!dependencyCheckSkip.value) {
      Def.task {
        val rules = (listParser.parsed match {
          case ParseResult.AllProjects => GenerateSuppressions.forAllProjects()
          case ParseResult.Aggregate => GenerateSuppressions.forAggregate()
          case ParseResult.PerProject | _ => GenerateSuppressions.forProject()
        }).value

        if (rules.nonEmpty) {
          log.info(s"Suppression rules added for [${name.value}]")
          rules.foreach(rule => log.info(s"\t${rule.toOwasp.toString}"))
        } else {
          log.info(s"No suppression rules added for [${name.value}]")
        }
      }
    } else {
      Def.task {
        log.info(s"Skipping dependency check for [${name.value}]")
      }
    }
  }

  private sealed abstract class ParseResult extends Product with Serializable
  private object ParseResult {
    case object PerProject extends ParseResult
    case object AllProjects extends ParseResult
    case object Aggregate extends ParseResult
  }

}
