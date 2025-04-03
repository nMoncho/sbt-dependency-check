/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import java.time.Duration

import scala.collection.Seq

import net.nmoncho.sbt.dependencycheck.settings._
import org.owasp.dependencycheck.reporting.ReportGenerator
import sbt._

object Keys {

  // Settings
  lazy val dependencyCheckFailBuildOnCVSS: SettingKey[Double] = settingKey(
    "Specifies if the build should be failed if a CVSS score above a specified level is identified. The default is 11 which means since the CVSS scores are 0-10, by default the build will never fail. More information on CVSS scores can be found at https://nvd.nist.gov/vuln-metrics/cvss"
  )
  lazy val dependencyCheckJUnitFailBuildOnCVSS: SettingKey[Option[Double]] =
    settingKey(
      "If using the jUnit, specifies the CVSS score that is considered a \"test\" failure when generating a jUnit style report. The default value is 0 - all vulnerabilities are considered a failure."
    )
  lazy val dependencyCheckSkip: SettingKey[Boolean] = settingKey(
    "Skips this project on the dependency-check analysis."
  )
  lazy val dependencyCheckScopes: SettingKey[ScopesSettings] = settingKey(
    "What library dependency scopes are considered during the analysis."
  )
  lazy val dependencyCheckScanSet: SettingKey[Seq[File]] = settingKey(
    "An optional sequence of files that specify additional files and/or directories to analyze as part of the scan. If not specified, defaults to standard scala conventions."
  )
  lazy val dependencyCheckFormats: SettingKey[Seq[ReportGenerator.Format]] = settingKey(
    "The report formats to be generated (HTML, XML, JUNIT, CSV, JSON, SARIF, JENKINS, ALL)."
  )

  lazy val dependencyCheckAnalysisTimeout: SettingKey[Option[Duration]] =
    settingKey("Set the analysis timeout.")

  lazy val dependencyCheckOutputDirectory: SettingKey[File] =
    settingKey("The location to write the report(s).")

  lazy val dependencyCheckAutoUpdate: SettingKey[Boolean] = settingKey(
    "Sets whether auto-updating of the NVD CVE/CPE, retireJS and hosted suppressions data is enabled. It is not recommended that this be turned to false."
  )
  lazy val dependencyCheckSettingsFile: SettingKey[File] = settingKey(
    "Where to look for the 'dependencycheck.properties' file. If this file exists, it will act as default, which values can be overridden by other SBT SettingKeys. Can be an external file, or a resource."
  )
  lazy val dependencyCheckAnalyzers: SettingKey[AnalyzerSettings] = settingKey(
    "Settings for the different analyzers used during the analysis."
  )
  lazy val dependencyCheckSuppressions: SettingKey[SuppressionSettings] = settingKey(
    "Combines a sequence of file paths, or URLs to the XML suppression files, with any hosted suppressions the analysis should be using. Suppressions are used to ignore false positives."
  )
  lazy val dependencyCheckDataDirectory: SettingKey[Option[File]] = settingKey(
    "Base path to use for the data directory (for embedded db and other cached resources from the Internet)"
  )
  lazy val dependencyCheckDatabase: SettingKey[DatabaseSettings] = settingKey(
    "Settings for the database used to hold the CVEs during the analysis."
  )
  lazy val dependencyCheckNvdApi: SettingKey[NvdApiSettings] = settingKey(
    "Settings to contact the NVD API, such as API Key, Request Delay, Max Retries, etc."
  )
  lazy val dependencyCheckProxy: SettingKey[ProxySettings] = settingKey(
    "Settings to use a Proxy. Honors System Properties like `https.proxyHost`, `https.proxyPort`, etc."
  )
  lazy val dependencyCheckConnectionTimeout: SettingKey[Option[Duration]] = settingKey(
    "Sets the URL Connection Timeout (in milliseconds) used when downloading external data."
  )
  lazy val dependencyCheckConnectionReadTimeout: SettingKey[Option[Duration]] = settingKey(
    "Sets the URL Connection Read Timeout (in milliseconds) used when downloading external data."
  )

  // Tasks
  lazy val dependencyCheck: TaskKey[Unit] = taskKey(
    "Runs dependency-check against the project and generates a report per sub project."
  )
  lazy val dependencyCheckAggregate: TaskKey[Unit] = taskKey(
    "Runs dependency-check against project aggregates and combines the results into a single report."
  )
  lazy val dependencyCheckAllProjects: TaskKey[Unit] = taskKey(
    "Runs dependency-check against all projects and combines the results into a single report."
  )
  lazy val dependencyCheckUpdate: TaskKey[Unit] =
    taskKey("Updates the local cache of the NVD data from NIST.")
  lazy val dependencyCheckPurge: TaskKey[Unit] =
    taskKey("Deletes the local copy of the NVD. This is used to force a refresh of the data.")
  lazy val dependencyCheckListSettings: TaskKey[Unit] =
    taskKey("List the settings used during the analysis.")
  lazy val dependencyCheckListUnusedSuppressions: TaskKey[Unit] =
    taskKey(
      "List unused suppressions, only considering suppression files, not hosted suppressions nor packed suppressions."
    )
  lazy val dependencyCheckListSuppressions: InputKey[Unit] = inputKey(
    "List suppression rules added to the Owasp Engine which are defined in the project definition (ie. build.sbt), or are imported packaged suppressions."
  )
}
