/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
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
