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

import java.time.Duration

import scala.collection.Seq

import net.nmoncho.sbt.dependencycheck.settings.AnalyzerSettings
import net.nmoncho.sbt.dependencycheck.settings.DatabaseSettings
import net.nmoncho.sbt.dependencycheck.settings.HostedSuppressionsSettings
import net.nmoncho.sbt.dependencycheck.settings.NvdApiSettings
import net.nmoncho.sbt.dependencycheck.settings.ProxySettings
import net.nmoncho.sbt.dependencycheck.settings.ScopesSettings
import net.nmoncho.sbt.dependencycheck.settings.SuppressionFilesSettings
import org.owasp.dependencycheck.reporting.ReportGenerator
import sbt.File
import sbt.SettingKey
import sbt.TaskKey
import sbt.settingKey
import sbt.taskKey

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
  lazy val dependencyCheckAnalyzer: SettingKey[AnalyzerSettings] = settingKey(
    "Settings for the different analyzers used during the analysis."
  )
  lazy val dependencyCheckSuppressionFiles: SettingKey[SuppressionFilesSettings] = settingKey(
    "The sequence of file paths or URLs to the XML suppression files - used to suppress false positives."
  )
  lazy val dependencyCheckDataDirectory: SettingKey[Option[File]] = settingKey(
    "Base path to use for the data directory (for embedded db and other cached resources from the Internet)"
  )
  lazy val dependencyCheckDatabase: SettingKey[DatabaseSettings] = settingKey(
    "Settings for the database used to hold the CVEs during the analysis."
  )
  lazy val dependencyCheckHostedSuppressions: SettingKey[HostedSuppressionsSettings] = settingKey(
    "Settings for any hosted suppressions the analysis should be using."
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

}
