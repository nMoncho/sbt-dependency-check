/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import java.time.Duration

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

/** Round-trip tests for the settings-to-OWASP mapping layer.
  *
  * Each `*.configure(settings)` method hand-writes the mapping from a settings field to an OWASP
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
      analyzer.configure(settings)

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
    val database = DatabaseSettings(
      driverName         = Some("org.postgresql.Driver"),
      driverPath         = Some("/opt/drivers/postgresql.jar"),
      connectionString   = Some("jdbc:postgresql://localhost/odc"),
      username           = Some("db-user"),
      password           = Some("db-pass"),
      batchInsertEnabled = Some(false),
      batchInsertSize    = Some(500)
    )

    val settings = new Settings()
    try {
      database.configure(settings)

      assertEquals(settings.getString(DB_DRIVER_NAME), "org.postgresql.Driver", "driver name")
      assertEquals(
        settings.getString(DB_DRIVER_PATH),
        "/opt/drivers/postgresql.jar",
        "driver path"
      )
      assertEquals(
        settings.getString(DB_CONNECTION_STRING),
        "jdbc:postgresql://localhost/odc",
        "connection string"
      )
      assertEquals(settings.getString(DB_USER), "db-user", "db username")
      assertEquals(settings.getString(DB_PASSWORD), "db-pass", "db password")
      assertEquals(settings.getString(ENABLE_BATCH_UPDATES), "false", "batch insert enabled")
      assertEquals(settings.getString(MAX_BATCH_SIZE), "500", "batch insert size")
    } finally settings.cleanup(true)
  }

  test("AnalyzerSettings.apply maps every top-level toggle to the correct OWASP key") {
    // Each toggle is set to the opposite of its OWASP default so that a wrong or missing key
    // constant (which would leave the default in place) is detected.
    val analyzer = AnalyzerSettings(
      additionalZipExtensions         = Some(Seq("war", "ear")),
      archiveEnabled                  = Some(false),
      autoconfEnabled                 = Some(false),
      cmakeEnabled                    = Some(false),
      cpanFileEnabled                 = Some(false),
      cpeEnabled                      = Some(false),
      cpeSuppressionEnabled           = Some(false),
      dartEnabled                     = Some(false),
      dependencyBundlingEnabled       = Some(false),
      dependencyMergingEnabled        = Some(false),
      experimentalEnabled             = Some(true),
      failOnUnusedSuppressionRule     = Some(true),
      falsePositiveEnabled            = Some(false),
      filenameEnabled                 = Some(false),
      fileVersionEnabled              = Some(false),
      jarEnabled                      = Some(false),
      mavenInstallEnabled             = Some(false),
      nvdCveEnabled                   = Some(false),
      openSslEnabled                  = Some(false),
      retiredEnabled                  = Some(true),
      versionFilterEnabled            = Some(false),
      vulnerabilitySuppressionEnabled = Some(false)
    )

    val settings = new Settings()
    try {
      analyzer.configure(settings)

      val expected = Seq(
        ADDITIONAL_ZIP_EXTENSIONS -> "war,ear",
        ANALYZER_ARCHIVE_ENABLED -> "false",
        ANALYZER_AUTOCONF_ENABLED -> "false",
        ANALYZER_CMAKE_ENABLED -> "false",
        ANALYZER_CPANFILE_ENABLED -> "false",
        ANALYZER_CPE_ENABLED -> "false",
        ANALYZER_CPE_SUPPRESSION_ENABLED -> "false",
        ANALYZER_DART_ENABLED -> "false",
        ANALYZER_DEPENDENCY_BUNDLING_ENABLED -> "false",
        ANALYZER_DEPENDENCY_MERGING_ENABLED -> "false",
        ANALYZER_EXPERIMENTAL_ENABLED -> "true",
        FAIL_ON_UNUSED_SUPPRESSION_RULE -> "true",
        ANALYZER_FALSE_POSITIVE_ENABLED -> "false",
        ANALYZER_FILE_NAME_ENABLED -> "false",
        ANALYZER_PE_ENABLED -> "false",
        ANALYZER_JAR_ENABLED -> "false",
        ANALYZER_MAVEN_INSTALL_ENABLED -> "false",
        ANALYZER_NVD_CVE_ENABLED -> "false",
        ANALYZER_OPENSSL_ENABLED -> "false",
        ANALYZER_RETIRED_ENABLED -> "true",
        ANALYZER_VERSION_FILTER_ENABLED -> "false",
        ANALYZER_VULNERABILITY_SUPPRESSION_ENABLED -> "false"
      )

      expected.foreach { case (key, value) =>
        assertEquals(settings.getString(key), value, key)
      }
    } finally settings.cleanup(true)
  }

  test("NvdApiSettings.apply maps every field, including the requestDelay to millis conversion") {
    val nvd = NvdApiSettings(
      apiKey         = "nvd-key",
      endpoint       = Some("https://nvd.example/api"),
      requestDelay   = Some(Duration.ofSeconds(2)),
      maxRetryCount  = Some(7),
      validForHours  = Some(12),
      resultsPerPage = Some(500),
      dataFeed       = NvdApiSettings.DataFeed(
        url          = Some(url("https://feed.example/nvd")),
        startYear    = Some(2005),
        validForDays = Some(9),
        username     = Some("feed-user"),
        password     = Some("feed-pass"),
        bearerToken  = Some("feed-token")
      )
    )

    val settings = new Settings()
    try {
      nvd.configure(settings)

      assertEquals(settings.getString(NVD_API_KEY), "nvd-key", "nvd api key")
      assertEquals(settings.getString(NVD_API_ENDPOINT), "https://nvd.example/api", "nvd endpoint")
      // 2 seconds must be stored as 2000 milliseconds
      assertEquals(settings.getString(NVD_API_DELAY), "2000", "nvd request delay (millis)")
      assertEquals(settings.getString(NVD_API_MAX_RETRY_COUNT), "7", "nvd max retry count")
      assertEquals(settings.getString(NVD_API_VALID_FOR_HOURS), "12", "nvd valid for hours")
      assertEquals(settings.getString(NVD_API_RESULTS_PER_PAGE), "500", "nvd results per page")

      assertEquals(
        settings.getString(NVD_API_DATAFEED_URL),
        "https://feed.example/nvd",
        "datafeed url"
      )
      assertEquals(settings.getString(NVD_API_DATAFEED_START_YEAR), "2005", "datafeed start year")
      assertEquals(settings.getString(NVD_API_DATAFEED_VALID_FOR_DAYS), "9", "datafeed valid days")
      assertEquals(settings.getString(NVD_API_DATAFEED_USER), "feed-user", "datafeed username")
      assertEquals(settings.getString(NVD_API_DATAFEED_PASSWORD), "feed-pass", "datafeed password")
      assertEquals(
        settings.getString(NVD_API_DATAFEED_BEARER_TOKEN),
        "feed-token",
        "datafeed bearer token"
      )
    } finally settings.cleanup(true)
  }

  test("HostedSuppressionsSettings.apply maps every field to the correct OWASP key") {
    val hosted = HostedSuppressionsSettings(
      enabled       = Some(false),
      url           = Some(url("https://suppressions.example/hosted.xml")),
      forceUpdate   = Some(true),
      validForHours = Some(6),
      username      = Some("hosted-user"),
      password      = Some("hosted-pass"),
      bearerToken   = Some("hosted-token")
    )

    val settings = new Settings()
    try {
      hosted.configure(settings)

      assertEquals(settings.getString(HOSTED_SUPPRESSIONS_ENABLED), "false", "hosted enabled")
      assertEquals(
        settings.getString(HOSTED_SUPPRESSIONS_URL),
        "https://suppressions.example/hosted.xml",
        "hosted url"
      )
      assertEquals(
        settings.getString(HOSTED_SUPPRESSIONS_FORCEUPDATE),
        "true",
        "hosted forceupdate"
      )
      assertEquals(
        settings.getString(HOSTED_SUPPRESSIONS_VALID_FOR_HOURS),
        "6",
        "hosted valid for hours"
      )
      assertEquals(settings.getString(HOSTED_SUPPRESSIONS_USER), "hosted-user", "hosted username")
      assertEquals(
        settings.getString(HOSTED_SUPPRESSIONS_PASSWORD),
        "hosted-pass",
        "hosted password"
      )
      assertEquals(
        settings.getString(HOSTED_SUPPRESSIONS_BEARER_TOKEN),
        "hosted-token",
        "hosted bearer token"
      )
    } finally settings.cleanup(true)
  }
}
