/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.io.File
import java.time.Duration

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

/** Tests for [[LoadSettings.applyBaseSettings]], the aggregation point that applies the top-level
  * plugin settings onto the OWASP [[Settings]] and performs unit conversions (durations to
  * millis/minutes, CVSS to float).
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
}
