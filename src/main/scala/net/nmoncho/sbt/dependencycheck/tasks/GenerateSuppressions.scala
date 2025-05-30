/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package tasks

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckSkip
import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckSuppressions
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings
import net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings._
import org.owasp.dependencycheck.xml.suppression.SuppressionParser
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

/** Generates the XML Suppression File containing suppressions specified
  * using [[SuppressionRule]]s
  */
object GenerateSuppressions {

  lazy val forProject: Def.Initialize[Task[Set[SuppressionRule]]] =
    Def.taskDyn {
      if (
        !thisProject.value.autoPlugins.contains(JvmPlugin) || (dependencyCheckSkip ?? false).value
      )
        Def.task(Set.empty)
      else
        Def.task {
          implicit val log: Logger = streams.value.log
          val settings             = dependencyCheckSuppressions.value
          val dependencies         = AggregateCheck.dependencies().value

          val buildSuppressions            = settings.suppressions
          val importedPackagedSuppressions = collectImportedPackagedSuppressions(
            settings,
            dependencies
          )

          (buildSuppressions ++ importedPackagedSuppressions).toSet
        }
    }

  /** Collects all [[SuppressionRule]]s packaged on the libraries included in this project.
    *
    * @return list of all packaged [[SuppressionRule]]s
    */
  def collectImportedPackagedSuppressions(
      settings: SuppressionSettings,
      dependencies: Set[Attributed[File]]
  )(implicit log: Logger): Seq[SuppressionRule] =
    if (settings.packagedEnabled) {
      val allowedDependencies = dependencies.filter(settings.packagedFilter).toSeq

      IO.withTemporaryDirectory { tempDir =>
        val parser = new SuppressionParser
        allowedDependencies.flatMap { dependency =>
          IO.unzip(
            dependency.data,
            tempDir,
            (filename: String) => filename == PackagedSuppressionsFilename
          ).flatMap { file =>
            log.debug(s"Extracting packaged suppressions file from JAR [${file.name}]")

            parseSuppressionFile(parser, file)
              // Make all imported packaged suppressions "base", so they don't show on this project's reports.
              .map(_.copy(base = true))
          }
        }
      }
    } else {
      log.debug("Packaged suppressions rules disabled, skipping...")
      Seq.empty[SuppressionRule]
    }

  /** Creates a file containing [[SuppressionRule]]s defined in the [[net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings.files]] field
    * and the [[net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings.suppressions]] field.
    *
    * @return a sequence of files if packaged suppressions are enabled, empty otherwise.
    */
  def exportPackagedSuppressions(): Def.Initialize[Task[Seq[File]]] = Def.taskDyn {
    implicit val log: Logger     = streams.value.log
    val settings                 = dependencyCheckSuppressions.value
    val packagedSuppressionsFile = (Compile / resourceManaged).value / PackagedSuppressionsFilename

    if (settings.packagedEnabled) {
      Def.task {
        val generated = writeExportSuppressions(packagedSuppressionsFile, settings)

        if (generated) {
          Seq(packagedSuppressionsFile)
        } else {
          Seq.empty
        }
      }
    } else {
      log.info(
        "Packaged suppressions is disabled, skipping packaged suppression file generation..."
      )
      Def.task(Seq.empty)
    }
  }

  /** Writes the exported packaged suppression rules
    *
    * Packaged suppressions rules will only consider the rules defined in the
    * [[net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings.files]] field
    * and the [[net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings.suppressions]] field.
    *
    * @param file file to write to
    * @param settings rules settings, including files and
    * @return true if a file was generated, false if there was not suppression rule to write to the file
    */
  private[tasks] def writeExportSuppressions(file: File, settings: SuppressionSettings)(
      implicit log: Logger
  ): Boolean = {
    val parser = new SuppressionParser

    val buildSuppressions = settings.suppressions
    val suppressionsFiles = settings.files.files.flatMap { filePath =>
      val file = new File(filePath)
      if (file.exists()) {
        log.debug(s"Including suppressions rules from [${file.name}]")
        parseSuppressionFile(parser, file)
      } else {
        log.debug(s"Ignoring [$filePath] on packaged suppressions rules export process")
        None
      }
    }

    val rules = (buildSuppressions ++ suppressionsFiles).distinct.map(
      // Make all packaged suppressions "base", so they don't show on downstream projects' reports.
      _.copy(base = true)
    )

    if (rules.nonEmpty) {
      log.info(
        s"Generating packaged suppression file to [${file.getAbsolutePath}]"
      )

      IO.write(file, SuppressionRule.toSuppressionsXML(rules))

      true
    } else {
      log.info("No suppressions defined, skipping packaged suppression file generation...")
      false
    }
  }

  private[tasks] def parseSuppressionFile(parser: SuppressionParser, file: File)(
      implicit log: Logger
  ): Seq[SuppressionRule] =
    try {
      import scala.jdk.CollectionConverters.*

      parser.parseSuppressionRules(file).asScala.map(SuppressionRule.fromOwasp)
    } catch {
      case t: Throwable =>
        log.warn(
          s"Failed parsing suppression rules from file [${file.name}], skipping file..."
        )
        logThrowable(t)
        Seq.empty[SuppressionRule]
    }
}
