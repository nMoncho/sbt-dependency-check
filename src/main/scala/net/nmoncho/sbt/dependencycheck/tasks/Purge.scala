/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.io.File
import java.io.IOException
import java.nio.file.Files

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS
import sbt.Def
import sbt.Keys.streams
import sbt.Logger
import sbt.Task

/** Purges DependencyCheck Cached Web Data Sources
  */
object Purge {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log

    withEngine(engineSettings.value) { engine =>
      engine.purge()
      Purge(engine)
    }
  }

  def apply(engine: Engine)(implicit log: Logger): Unit = {
    val settings = engine.getSettings

    if (settings.getString(KEYS.DB_CONNECTION_STRING) == null) {
      throw new IllegalStateException(
        "Unable to purge the local NVD when using an empty connection string"
      )
    }

    try {
      log.info("Attempting cached web data sources purge")
      val successfulPurge = engine.purge()

      if (successfulPurge) {
        log.info("Cached web data sources purged successfully")
      } else {
        log.warn("Failed to purge cached web data sources")
      }

      // `Engine.purge()` iterates through its `CachedWebDataSource`s, since we're not sure what's inside
      // let's try to delete the DB in case `NvdApiDataSource.purge()` wasn't invoked
      val db = new File(settings.getDataDirectory, settings.getString(Settings.KEYS.DB_FILE_NAME))
      if (db.exists()) {
        log.info("Database file still exists after Engine purge, re-trying deletion")
        if (Files.deleteIfExists(db.toPath)) {
          log.info("Database file purged; local copy of the NVD has been removed")
        } else {
          log.error(s"Unable to delete '${db.getAbsolutePath}'; please delete the file manually")
        }
      } else if (!successfulPurge) {
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
