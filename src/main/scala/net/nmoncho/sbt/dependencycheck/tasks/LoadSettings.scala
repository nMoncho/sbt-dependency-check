/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package tasks

import java.io.File
import java.io.FileInputStream
import java.time.Duration

import scala.util.Using

import net.nmoncho.sbt.dependencycheck.Keys
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Def
import sbt.Keys.name
import sbt.Keys.streams
import sbt.Logger
import sbt.Task

object LoadSettings {

  private val autoImport: Keys.type = Keys

  import autoImport.*

  def apply(): Def.Initialize[Task[Settings]] = Def.task {
    implicit val log: Logger = streams.value.log

    val propertiesFile = dependencyCheckSettingsFile.value

    val baseSettings = loadBaseSettings(propertiesFile)

    applyBaseSettings(
      baseSettings,
      name.value,
      dependencyCheckAutoUpdate.value,
      dependencyCheckConnectionTimeout.value,
      dependencyCheckConnectionReadTimeout.value,
      dependencyCheckJUnitFailBuildOnCVSS.value,
      dependencyCheckAnalysisTimeout.value,
      dependencyCheckDataDirectory.value
    )

    dependencyCheckAnalyzers.value(baseSettings)
    dependencyCheckDatabase.value(baseSettings)
    dependencyCheckNvdApi.value(baseSettings)
    dependencyCheckSuppressions.value(baseSettings)

    baseSettings
  }

  /** Loads the base OWASP [[Settings]] from `dependencyCheckSettingsFile` (a file on disk, otherwise
    * a classpath resource of the same name), starting from OWASP's bundled defaults and merging the
    * user's properties on top.
    *
    * Starting from `new Settings()` (rather than `new Settings(props)`) preserves the built-in
    * defaults, in particular `odc.settings.mask`, so secrets stay masked in
    * `dependencyCheckListSettings`; `mergeProperties` makes an external properties file a partial
    * override rather than a full replacement of every default.
    */
  private[tasks] def loadBaseSettings(propertiesFile: File)(implicit log: Logger): Settings =
    Using {
      if (propertiesFile.exists()) new FileInputStream(propertiesFile)
      else getClass.getClassLoader.getResourceAsStream(propertiesFile.getPath)
    } { is =>
      val settings = new Settings()
      settings.mergeProperties(is)
      settings
    }.recover { case t: Throwable =>
      log.error(s"Failed to load 'dependencyCheckSettingsFile' at [$propertiesFile]")
      logThrowable(t)
      new Settings()
    }.get

  /** Applies the top-level plugin settings (name, auto-update, timeouts, data directory) onto the
    * OWASP [[Settings]], performing the required unit conversions.
    *
    * Extracted from [[apply]] so the conversions (durations to millis/minutes, CVSS to float) can be
    * unit-tested without driving an sbt task.
    */
  private[tasks] def applyBaseSettings(
      settings: Settings,
      name: String,
      autoUpdate: Boolean,
      connectionTimeout: Option[Duration],
      connectionReadTimeout: Option[Duration],
      junitFailBuildOnCVSS: Option[Double],
      analysisTimeout: Option[Duration],
      dataDirectory: Option[File]
  ): Unit = {
    settings.setStringIfNotEmpty(APPLICATION_NAME, name)
    settings.setBoolean(AUTO_UPDATE, autoUpdate)
    connectionTimeout.foreach(value => settings.setInt(CONNECTION_TIMEOUT, value.toMillis.toInt))
    connectionReadTimeout.foreach(value =>
      settings.setInt(CONNECTION_READ_TIMEOUT, value.toMillis.toInt)
    )
    junitFailBuildOnCVSS.foreach(value => settings.setFloat(JUNIT_FAIL_ON_CVSS, value.toFloat))
    analysisTimeout.foreach(value => settings.setInt(ANALYSIS_TIMEOUT, value.toMinutes.toInt))
    dataDirectory.foreach(folder =>
      settings.setStringIfNotEmpty(DATA_DIRECTORY, folder.getAbsolutePath)
    )
  }

}
