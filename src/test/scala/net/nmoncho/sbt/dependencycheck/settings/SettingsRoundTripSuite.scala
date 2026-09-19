/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import java.io.File

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

/** Round-trip tests for the settings-to-OWASP mapping layer.
  *
  * Each `*.apply(settings)` method hand-writes the mapping from a settings field to an OWASP
  * property key. Those mappings are easy to break by copy-paste (wrong field or wrong key), and
  * such a break silently misconfigures an analyzer rather than failing the build. These tests build
  * fully non-default settings, apply them to a real [[Settings]], and assert every key reads back
  * the configured value. Sibling values are deliberately distinct so a cross-wire is detectable.
  */
class SettingsRoundTripSuite extends munit.FunSuite {

  private def url(s: String) = new java.net.URI(s).toURL

  test("AnalyzerSettings.apply maps every field to the correct OWASP key") {
    val analyzer = AnalyzerSettings(
      // Golang: dep and mod must not be cross-wired (regression: both read from `moduleAnalyzerEnabled`)
      golang = AnalyzerSettings.Golang(
        dependencyAnalyzerEnabled = Some(false),
        moduleAnalyzerEnabled     = Some(true)
      ),
      // KEV: `validForHours` (Int) must not be overwritten by `url` (regression: url written to the int key)
      knownExploitedVulnerabilities = AnalyzerSettings.KnownExploitedVulnerabilities(
        url           = Some(url("https://kev.example/kev.json")),
        validForHours = Some(99)
      ),
      // Maven Central credentials must survive; Nexus must not overwrite them (regression: shared keys)
      mavenCentral = AnalyzerSettings.MavenCentral(
        username = Some("central-user"),
        password = Some("central-pass")
      ),
      // Nexus credentials must land on the Nexus keys (regression: written to Central keys)
      nexus = AnalyzerSettings.Nexus(
        username = Some("nexus-user"),
        password = Some("nexus-pass")
      ),
      // Node: `auditUsesCache` must not be cross-wired with `auditSkipDevDependencies`
      node = AnalyzerSettings.Node(
        auditSkipDevDependencies = Some(true),
        auditUsesCache           = Some(false)
      ),
      // PHP: `composerLockEnabled` must not be cross-wired with `composerLockSkipDevDependencies`
      php = AnalyzerSettings.Php(
        composerLockEnabled             = Some(false),
        composerLockSkipDevDependencies = Some(true)
      ),
      // Python: `pipFileEnabled` must not be cross-wired with `pipEnabled`
      python = AnalyzerSettings.Python(
        pipEnabled     = Some(false),
        pipFileEnabled = Some(true)
      )
    )

    val settings = new Settings()
    try {
      analyzer(settings)

      assertEquals(settings.getString(ANALYZER_GOLANG_DEP_ENABLED), "false", "golang dep analyzer")
      assertEquals(settings.getString(ANALYZER_GOLANG_MOD_ENABLED), "true", "golang mod analyzer")

      assertEquals(settings.getString(KEV_CHECK_VALID_FOR_HOURS), "99", "kev validForHours")
      assertEquals(settings.getString(KEV_URL), "https://kev.example/kev.json", "kev url")

      assertEquals(settings.getString(ANALYZER_CENTRAL_USER), "central-user", "central username")
      assertEquals(
        settings.getString(ANALYZER_CENTRAL_PASSWORD),
        "central-pass",
        "central password"
      )
      assertEquals(settings.getString(ANALYZER_NEXUS_USER), "nexus-user", "nexus username")
      assertEquals(settings.getString(ANALYZER_NEXUS_PASSWORD), "nexus-pass", "nexus password")

      assertEquals(settings.getString(ANALYZER_NODE_AUDIT_SKIPDEV), "true", "node audit skipdev")
      assertEquals(
        settings.getString(ANALYZER_NODE_AUDIT_USE_CACHE),
        "false",
        "node audit use cache"
      )

      assertEquals(
        settings.getString(ANALYZER_COMPOSER_LOCK_ENABLED),
        "false",
        "composer lock enabled"
      )
      assertEquals(
        settings.getString(ANALYZER_COMPOSER_LOCK_SKIP_DEV),
        "true",
        "composer lock skipdev"
      )

      assertEquals(settings.getString(ANALYZER_PIP_ENABLED), "false", "pip analyzer")
      assertEquals(settings.getString(ANALYZER_PIPFILE_ENABLED), "true", "pipfile analyzer")
    } finally settings.cleanup(true)
  }

  test("DatabaseSettings.apply maps every field to the correct OWASP key") {
    val dataDir  = new File("target/round-trip-data")
    val database = DatabaseSettings(
      driverName       = Some("org.postgresql.Driver"),
      connectionString = Some("jdbc:postgresql://localhost/odc"),
      username         = Some("db-user"),
      password         = Some("db-pass"),
      dataDirectory    = Some(dataDir),
      batchInsertSize  = Some(500)
    )

    val settings = new Settings()
    try {
      database(settings)

      assertEquals(settings.getString(DB_DRIVER_NAME), "org.postgresql.Driver", "driver name")
      assertEquals(
        settings.getString(DB_CONNECTION_STRING),
        "jdbc:postgresql://localhost/odc",
        "connection string"
      )
      assertEquals(settings.getString(DB_USER), "db-user", "db username")
      assertEquals(settings.getString(DB_PASSWORD), "db-pass", "db password")
      // Regression: `dataDirectory` was never applied to DATA_DIRECTORY
      assertEquals(settings.getString(DATA_DIRECTORY), dataDir.getAbsolutePath, "data directory")
      assertEquals(settings.getString(MAX_BATCH_SIZE), "500", "batch insert size")
    } finally settings.cleanup(true)
  }
}
