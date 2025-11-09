/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.nio.file.Files

import net.nmoncho.sbt.dependencycheck.DependencyCheckTestCompat
import net.nmoncho.sbt.dependencycheck.Utils.StringLogger
import net.nmoncho.sbt.dependencycheck.settings.SuppressionFilesSettings
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings
import net.nmoncho.sbt.dependencycheck.settings.SuppressionSettings.PackagedFilter
import org.owasp.dependencycheck.xml.suppression.SuppressionParser
import sbt.File
import sbt.Logger
import sbt.internal.util.Attributed
import sbt.librarymanagement.ModuleID

class GenerateSuppressionsSuite extends munit.FunSuite {

  private val suppressionFile = new File("src/test/resources/suppressions.xml")

  test("Packaged suppressions should be disabled by default, and blacklist all jars") {
    implicit val log: Logger = Logger.Null

    val settings     = SuppressionSettings.Default
    val suppressions = GenerateSuppressions.collectImportedPackagedSuppressions(
      settings,
      Set(
        attributedFile("net.nmoncho-foobar-1.23.jar", ("net.nmoncho", "foobar", "1.23")),
        attributedFile("nmoncho.net-barfoo-4.56.jar", ("moncho.net", "barfoo", "4.56"))
      )
    )

    assert(suppressions.isEmpty, "packaged suppressions are disabled by default")

    val otherSuppressions = GenerateSuppressions.collectImportedPackagedSuppressions(
      settings.copy(packagedEnabled = true),
      Set(
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
      Set(
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
        suppressions    = Seq(
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
      DependencyCheckTestCompat.attributeMap(
        ModuleID(gav._1, gav._1, gav._3)
      )
    )
}
