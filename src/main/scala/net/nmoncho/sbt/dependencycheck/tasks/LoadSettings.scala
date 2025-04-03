/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package tasks

import java.io.FileInputStream
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

    baseSettings.setStringIfNotEmpty(APPLICATION_NAME, name.value)
    baseSettings.setBoolean(AUTO_UPDATE, dependencyCheckAutoUpdate.value)
    dependencyCheckConnectionTimeout.value.foreach(value =>
      baseSettings.setInt(CONNECTION_TIMEOUT, value.toMillis.toInt)
    )
    dependencyCheckConnectionReadTimeout.value.foreach(value =>
      baseSettings.setInt(CONNECTION_READ_TIMEOUT, value.toMillis.toInt)
    )
    dependencyCheckJUnitFailBuildOnCVSS.value.foreach(value =>
      baseSettings.setFloat(JUNIT_FAIL_ON_CVSS, value.toFloat)
    )
    dependencyCheckAnalysisTimeout.value.foreach(value =>
      baseSettings.setInt(ANALYSIS_TIMEOUT, value.toMinutes.toInt)
    )
    dependencyCheckDataDirectory.value.foreach(folder =>
      baseSettings.setStringIfNotEmpty(DATA_DIRECTORY, folder.getAbsolutePath)
    )

    dependencyCheckAnalyzers.value(baseSettings)
    dependencyCheckDatabase.value(baseSettings)
    dependencyCheckNvdApi.value(baseSettings)
    dependencyCheckSuppressions.value(baseSettings)

    baseSettings
  }

}
