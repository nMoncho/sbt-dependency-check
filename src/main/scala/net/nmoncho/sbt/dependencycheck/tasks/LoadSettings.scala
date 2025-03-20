package net.nmoncho.sbt.dependencycheck
package tasks

import net.nmoncho.sbt.dependencycheck.Keys
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS.*
import sbt.Keys.streams
import sbt.{ Def, Logger, Task }

import java.io.FileInputStream
import java.util.Properties
import scala.util.Using

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
