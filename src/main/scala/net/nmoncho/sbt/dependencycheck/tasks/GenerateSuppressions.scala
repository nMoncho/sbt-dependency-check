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

package net.nmoncho.sbt.dependencycheck
package tasks

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckSuppressions
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings._
import org.owasp.dependencycheck.xml.suppression.SuppressionParser
import sbt.Keys._
import sbt._

/** Generates the XML Suppression File containing suppressions specified
  * using [[SuppressionRule]]s
  */
object GenerateSuppressions {

  def apply(): Def.Initialize[Task[File]] = Def.taskDyn {
    implicit val log: Logger = streams.value.log
    val settings             = dependencyCheckSuppressions.value
    val combinedSuppressions = crossTarget.value / CombinedSuppressionsFilename

    val buildSuppressions            = settings.suppressions
    val importedPackagedSuppressions = collectImportedPackagedSuppressions().value
    val suppressions                 = buildSuppressions ++ importedPackagedSuppressions

    if (suppressions.nonEmpty) {
      log.info(
        s"Generating suppression file to [${combinedSuppressions.getAbsolutePath}]"
      )

      Def.task {
        IO.write(
          combinedSuppressions,
          SuppressionRule.toSuppressionsXML(suppressions)
        )

        combinedSuppressions
      }
    } else {
      log.debug("No suppressions defined, skipping suppression file generation...")
      Def.task(combinedSuppressions)
    }
  }

  /** Collects all [[SuppressionRule]]s packaged on the libraries included in this project.
    *
    * @return list of all packaged [[SuppressionRule]]s
    */
  def collectImportedPackagedSuppressions(): Def.Initialize[Task[Seq[SuppressionRule]]] =
    Def.taskDyn {
      implicit val log: Logger = streams.value.log
      val settings             = dependencyCheckSuppressions.value

      if (settings.packagedEnabled) {
        Def.task {
          val dependencies =
            (Compile / externalDependencyClasspath).value.filter(settings.packagedFilter)

          IO.withTemporaryDirectory { tempDir =>
            val parser = new SuppressionParser
            dependencies.flatMap { dependency =>
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
        }
      } else {
        log.debug("Packaged suppressions rules disabled, skipping...")
        Def.task(Seq.empty[SuppressionRule])
      }
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
    val parser                   = new SuppressionParser

    // Suppression files are parsed lazily in case `packagedEnabled` is disabled
    val suppressionsFiles = if (settings.packagedEnabled) {
      settings.files.files.flatMap { filePath =>
        val file = new File(filePath)
        if (file.exists()) {
          log.debug(s"Including suppressions rules from [${file.name}]")
          parseSuppressionFile(parser, file)
        } else {
          log.debug(s"Ignoring [$filePath] on packaged suppressions rules export process")
          None
        }
      }
    } else {
      Seq.empty
    }

    val buildSuppressions = settings.suppressions
    val suppressions      = buildSuppressions ++ suppressionsFiles

    if (settings.packagedEnabled && suppressions.nonEmpty) {
      log.info(
        s"Generating packaged suppression file to [${packagedSuppressionsFile.getAbsolutePath}]"
      )

      Def.task {
        IO.write(
          packagedSuppressionsFile,
          SuppressionRule.toSuppressionsXML(
            // Make all packaged suppressions "base", so they don't show on downstream projects' reports.
            suppressions.map(_.copy(base = true))
          )
        )

        Seq(packagedSuppressionsFile)
      }
    } else {
      log.info(
        "Either packaged suppressions is disabled, or there are no suppressions defined, skipping packaged suppression file generation..."
      )
      Def.task(Seq.empty)
    }
  }

  private def parseSuppressionFile(parser: SuppressionParser, file: File)(
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
