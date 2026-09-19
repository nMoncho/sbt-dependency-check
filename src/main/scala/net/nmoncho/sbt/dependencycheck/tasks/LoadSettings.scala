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
import java.util.Properties

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

    val baseSettings = Using {
      if (propertiesFile.exists()) new FileInputStream(propertiesFile)
      else getClass.getClassLoader.getResourceAsStream(propertiesFile.getPath)
    } { is =>
      val props = new Properties()
      props.load(is)

      new Settings(props)
    }.recover { case t: Throwable =>
      log.error(s"Failed to load 'dependencyCheckSettingsFile' at [$propertiesFile]")
      logThrowable(t)
      new Settings()
    }.get

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
