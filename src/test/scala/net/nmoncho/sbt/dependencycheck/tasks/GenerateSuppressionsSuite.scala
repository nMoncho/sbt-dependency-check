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

import java.nio.file.Files

import net.nmoncho.sbt.dependencycheck.Utils.StringLogger
import net.nmoncho.sbt.dependencycheck.settings.SuppressionFilesSettings
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings
import net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings.PackagedFilter
import org.owasp.dependencycheck.xml.suppression.SuppressionParser
import sbt.File
import sbt.Keys
import sbt.Logger
import sbt.internal.util.AttributeEntry
import sbt.internal.util.AttributeMap
import sbt.internal.util.Attributed
import sbt.librarymanagement.ModuleID

class GenerateSuppressionsSuite extends munit.FunSuite {

  private val suppressionFile = new File("src/test/resources/suppressions.xml")

  test("Packaged suppressions should be disabled by default, and blacklist all jars") {
    implicit val log: Logger = Logger.Null

    val settings = SuppressionSettings.Default
    val suppressions = GenerateSuppressions.collectImportedPackagedSuppressions(
      settings,
      Seq(
        attributedFile("net.nmoncho-foobar-1.23.jar", ("net.nmoncho", "foobar", "1.23")),
        attributedFile("nmoncho.net-barfoo-4.56.jar", ("moncho.net", "barfoo", "4.56"))
      )
    )

    assert(suppressions.isEmpty, "packaged suppressions are disabled by default")

    val otherSuppressions = GenerateSuppressions.collectImportedPackagedSuppressions(
      settings.copy(packagedEnabled = true),
      Seq(
        attributedFile("net.nmoncho-foobar-1.23.jar", ("net.nmoncho", "foobar", "1.23")),
        attributedFile("nmoncho.net-barfoo-4.56.jar", ("moncho.net", "barfoo", "4.56"))
      )
    )

    assert(otherSuppressions.isEmpty, "packaged suppressions blacklist all jars by default")
  }

  test("Packaged suppressions should importable and usable") {
    implicit val log: Logger = Logger.Null

    val settings = SuppressionSettings(
      packagedEnabled = true,
      packagedFilter  = PackagedFilter.ofGav((groupId, _, _) => groupId == "net.nmoncho")
    )

    val suppressions = GenerateSuppressions.collectImportedPackagedSuppressions(
      settings,
      Seq(
        attributedFile("net.nmoncho-foobar-1.23.jar", ("net.nmoncho", "foobar", "1.23")),
        attributedFile("nmoncho.net-barfoo-4.56.jar", ("moncho.net", "barfoo", "4.56"))
      )
    )

    assert(suppressions.nonEmpty, "suppressions should be imported")
    assert(
      suppressions.exists(_.notes == "Some packaged suppression for commons-cli"),
      "'net.nmoncho-foobar-1.23.jar' is filtered in due to its GAV"
    )
    assert(
      !suppressions.exists(_.cpe.exists(_.value == "cpe:/a:python:python")),
      "'nmoncho.net-barfoo-4.56.jar' is filtered out"
    )

  }

  test("Packaged suppressions should exportable and usable") {
    implicit val log: Logger = Logger.Null

    val tmpFolder = Files.createTempDirectory(null)
    val exported  = new File(tmpFolder.toFile, "exported.xml")

    val generated = GenerateSuppressions.writeExportSuppressions(
      exported,
      SuppressionSettings(
        packagedEnabled = true,
        files           = SuppressionFilesSettings.files()(suppressionFile),
        suppressions = Seq(
          SuppressionRule(cvssBelow = Seq(10.0))
        )
      )
    )

    assert(generated, "generation should be successful")
    assert(exported.exists(), "packaged file should exist")

    // Parse exported rules so we know they should work when picked up
    val parsed = GenerateSuppressions.parseSuppressionFile(
      new SuppressionParser,
      exported
    )

    assert(parsed.nonEmpty, "packaged rules should be parseable")
    assertEquals(parsed.size, 867)
  }

  test("Suppression files are parsed and converted properly") {
    implicit val log: Logger = Logger.Null

    val rules = GenerateSuppressions.parseSuppressionFile(
      new SuppressionParser,
      suppressionFile
    )

    assert(rules.nonEmpty)
    assertEquals(rules.size, 866)
  }

  test("Suppression files parsing failures are reported") {
    implicit val log: StringLogger = new StringLogger

    val rules = GenerateSuppressions.parseSuppressionFile(
      new SuppressionParser,
      new File("src/test/resources/malformed-suppressions.xml")
    )

    assert(rules.isEmpty, "on parsing failure, an empty suppression list should be returned")

    val logs = log.sb.result()
    assert(
      logs.contains(
        "Failed parsing suppression rules from file [malformed-suppressions.xml], skipping file..."
      )
    )
  }

  private def attributedFile(path: String, gav: (String, String, String)): Attributed[File] =
    Attributed(new File(s"src/test/resources/$path"))(
      AttributeMap(AttributeEntry(Keys.moduleID.key, ModuleID(gav._1, gav._1, gav._3)))
    )
}
