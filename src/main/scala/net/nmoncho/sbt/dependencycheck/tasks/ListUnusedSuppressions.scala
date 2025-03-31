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

import scala.jdk.CollectionConverters.collectionAsScalaIterableConverter

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.scanSet
import net.nmoncho.sbt.dependencycheck.Keys._
import net.nmoncho.sbt.dependencycheck.VulnerabilityFoundException
import org.owasp.dependencycheck.analyzer.AbstractSuppressionAnalyzer.SUPPRESSION_OBJECT_KEY
import org.owasp.dependencycheck.xml.suppression.SuppressionRule
import sbt.Keys._
import sbt._

object ListUnusedSuppressions {

  def apply(): Def.Initialize[Task[Unit]] = Def.taskDyn {
    implicit val log: Logger = streams.value.log

    if (!dependencyCheckSkip.value) {
      Def.task {
        log.info(s"Running check for [${name.value}]")

        val failCvssScore    = dependencyCheckFailBuildOnCVSS.value
        val scanSetFiles     = scanSet.value
        val dependencies     = Dependencies.projectDependencies.value
        val suppressionRules = GenerateSuppressions.forProject().value

        log.info("Scanning following dependencies: ")
        dependencies.foreach(f => log.info("\t" + f.data.getName))

        withEngine(engineSettings.value) { engine =>
          try {
            analyzeProject(
              name.value,
              engine,
              dependencies,
              suppressionRules,
              scanSetFiles,
              failCvssScore,
              dependencyCheckOutputDirectory.value,
              dependencyCheckFormats.value
            )
          } catch {
            case _: VulnerabilityFoundException => // ignore
          }

          val unusedSuppressions = engine
            .getObject(SUPPRESSION_OBJECT_KEY)
            .asInstanceOf[java.util.List[SuppressionRule]]
            .asScala
            .filter(sup => !sup.isMatched && !sup.isBase)

          if (unusedSuppressions.nonEmpty) {
            log.info(s"""
                 |
                 |Found [${unusedSuppressions.size}] unused suppressions for project [${name.value}]:
                 |${unusedSuppressions.mkString("\n\t", "\n\t", "\n")}
                 |
                 |""".stripMargin)
          } else {
            log.info("No unused suppressions.")
          }
        }
      }
    } else {
      Def.task {
        log.info(s"Skipping dependency check for [${name.value}]")
      }
    }
  }

}
