/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.Duration

import net.nmoncho.sbt.dependencycheck.ConfigurationException
import net.nmoncho.sbt.dependencycheck.Utils.StringLogger
import net.nmoncho.sbt.dependencycheck.settings.ScopesSettings
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

/** Tests for [[LoadSettings]]: `applyBaseSettings` (the aggregation point that applies the top-level
  * plugin settings and performs unit conversions) and `loadBaseSettings` (which must preserve the
  * OWASP secret mask when a user properties file is supplied).
  */
class LoadSettingsSuite extends munit.FunSuite {

  test("applyBaseSettings applies all provided values with the correct unit conversions") {
    val settings = new Settings()
    try {
      LoadSettings.applyBaseSettings(
        settings,
        name                  = "my-project",
        autoUpdate            = false,
        connectionTimeout     = Some(Duration.ofSeconds(30)),
        connectionReadTimeout = Some(Duration.ofSeconds(45)),
        junitFailBuildOnCVSS  = Some(7.5),
        analysisTimeout       = Some(Duration.ofMinutes(3)),
        dataDirectory         = Some(new File("target/load-settings-data"))
      )

      assertEquals(settings.getString(APPLICATION_NAME), "my-project", "application name")
      assertEquals(settings.getString(AUTO_UPDATE), "false", "auto update")
      // 30 seconds -> 30000 milliseconds
      assertEquals(settings.getString(CONNECTION_TIMEOUT), "30000", "connection timeout (millis)")
      // 45 seconds -> 45000 milliseconds
      assertEquals(
        settings.getString(CONNECTION_READ_TIMEOUT),
        "45000",
        "connection read timeout (millis)"
      )
      assertEquals(settings.getString(JUNIT_FAIL_ON_CVSS), "7.5", "junit fail on cvss")
      // 3 minutes -> 3 (the setting is expressed in minutes)
      assertEquals(settings.getString(ANALYSIS_TIMEOUT), "3", "analysis timeout (minutes)")
      assertEquals(
        settings.getString(DATA_DIRECTORY),
        new File("target/load-settings-data").getAbsolutePath,
        "data directory"
      )
    } finally settings.cleanup(true)
  }

  test("applyBaseSettings leaves OWASP defaults untouched when optional values are absent") {
    val settings = new Settings()
    try {
      LoadSettings.applyBaseSettings(
        settings,
        name                  = "",
        autoUpdate            = true,
        connectionTimeout     = None,
        connectionReadTimeout = None,
        junitFailBuildOnCVSS  = None,
        analysisTimeout       = None,
        dataDirectory         = None
      )

      // An empty name must not overwrite the OWASP default (setStringIfNotEmpty)
      assertEquals(settings.getString(APPLICATION_NAME), "Dependency-Check Core", "default name")
      // Absent optionals leave the OWASP defaults in place
      assertEquals(settings.getString(CONNECTION_TIMEOUT), null, "connection timeout default")
      assertEquals(settings.getString(ANALYSIS_TIMEOUT), "180", "analysis timeout default")
      assertEquals(settings.getString(JUNIT_FAIL_ON_CVSS), "0", "junit fail on cvss default")
    } finally settings.cleanup(true)
  }

  test("loadBaseSettings keeps the secret mask when a user properties file omits it") {
    implicit val log: StringLogger = new StringLogger

    // A user-supplied properties file that carries a secret but does NOT define odc.settings.mask.
    val file = Files.createTempFile("dependencycheck", ".properties")
    Files.write(file, "nvd.api.key=super-secret-key\n".getBytes(StandardCharsets.UTF_8))

    val settings = LoadSettings.loadBaseSettings(file.toFile)
    try {
      // The user's override is applied...
      assertEquals(settings.getString(NVD_API_KEY), "super-secret-key", "user override applied")
      // ...and the bundled defaults survive (merge, not replace)...
      assertEquals(settings.getString(DB_DRIVER_NAME), "org.h2.Driver", "bundled default preserved")
      // ...including the mask, so the key is not empty.
      assert(
        Option(settings.getArray(MASKED_PROPERTIES)).exists(_.nonEmpty),
        "odc.settings.mask must be preserved from the bundled defaults"
      )

      // And dependencyCheckListSettings must mask, not print, the secret.
      ListSettings(settings, ScopesSettings.Default)
      val output = log.sb.result()
      assert(output.contains("nvd.api.key: ********"), s"nvd.api.key must be masked:\n$output")
      assert(!output.contains("super-secret-key"), "the secret value must never be printed")
    } finally {
      settings.cleanup(true)
      Files.deleteIfExists(file)
    }
  }

  test("loadBaseSettings fails fast when a present settings file cannot be read") {
    implicit val log: StringLogger = new StringLogger

    // A directory exists but cannot be opened as a properties file: the load must fail loudly
    // instead of silently continuing with defaults.
    val dir = Files.createTempDirectory("dependencycheck-settings").toFile
    try {
      intercept[ConfigurationException] {
        LoadSettings.loadBaseSettings(dir)
      }
    } finally dir.delete()
  }

  test("loadBaseSettings falls back to defaults when no file or resource is found") {
    implicit val log: StringLogger = new StringLogger

    val missing  = new File("does-not-exist-dependencycheck-xyz.properties")
    val settings = LoadSettings.loadBaseSettings(missing)
    try {
      // No exception, and the OWASP defaults (including the mask) are present.
      assert(
        Option(settings.getString(DB_DRIVER_NAME)).contains("org.h2.Driver"),
        "OWASP defaults should be used when no settings file is found"
      )
      assert(log.sb.result().contains("continuing with OWASP defaults"), "the fallback is surfaced")
    } finally settings.cleanup(true)
  }
}
