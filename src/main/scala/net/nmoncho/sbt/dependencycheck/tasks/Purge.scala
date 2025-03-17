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

package net.nmoncho.sbt.dependencycheck.tasks

import java.io.File
import java.io.IOException
import java.nio.file.Files

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS
import sbt.Def
import sbt.Keys.streams
import sbt.Logger
import sbt.Task

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
