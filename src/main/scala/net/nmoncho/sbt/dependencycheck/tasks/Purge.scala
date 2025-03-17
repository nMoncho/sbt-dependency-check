package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS
import sbt.Keys.streams
import sbt.{ Def, Logger, Task }

import java.io.{ File, IOException }
import java.nio.file.Files

object Purge {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log

    withEngine(engineSettings.value) { engine =>
      Purge(engine.getSettings)
    }
  }

  def apply(settings: Settings)(implicit log: Logger): Unit = {
    if (settings.getString(KEYS.DB_CONNECTION_STRING) == null) {
      throw new IllegalStateException(
        "Unable to purge the local NVD when using a non-default connection string"
      )
    }

    try {
      val db = new File(settings.getDataDirectory, settings.getString(Settings.KEYS.DB_FILE_NAME))
      if (db.exists()) {
        if (Files.deleteIfExists(db.toPath)) {
          log.info("Database file purged; local copy of the NVD has been removed")
        } else {
          log.error(s"Unable to delete '${db.getAbsolutePath}'; please delete the file manually")
        }
      } else {
        log.error(s"Unable to delete '${db.getAbsolutePath}'; the database file does not exists")
      }
    } catch {
      case e: IOException =>
        log.error(s"Can't purge NVD database: ${e.getLocalizedMessage}")
        throw e
    } finally {
      settings.cleanup()
    }
  }

}
