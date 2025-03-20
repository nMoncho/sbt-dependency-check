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
package tasks

import java.io.FileInputStream
import java.util.Properties

import scala.util.Using

import net.nmoncho.sbt.dependencycheck.Keys
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.Def
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

    baseSettings.setBoolean(AUTO_UPDATE, dependencyCheckAutoUpdate.value)
    dependencyCheckConnectionTimeout.value.foreach(
      baseSettings.setInt(CONNECTION_TIMEOUT, _)
    )
    dependencyCheckConnectionReadTimeout.value.foreach(
      baseSettings.setInt(CONNECTION_READ_TIMEOUT, _)
    )
    dependencyCheckJUnitFailBuildOnCVSS.value.foreach(value =>
      baseSettings.setFloat(JUNIT_FAIL_ON_CVSS, value.toFloat)
    )
    dependencyCheckAnalysisTimeout.value.foreach(value =>
      baseSettings.setInt(ANALYSIS_TIMEOUT, value.toMinutes.toInt)
    )

    dependencyCheckAnalyzer.value(baseSettings)
    dependencyCheckDatabase.value(baseSettings)
    dependencyCheckNvdApi.value(baseSettings)
    dependencyCheckSuppressionFiles.value(baseSettings)
    dependencyCheckHostedSuppressions.value(baseSettings)

    baseSettings
  }

}
