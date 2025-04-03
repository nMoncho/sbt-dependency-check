/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.nio.file.Files
import java.util.Properties
import java.util.UUID

import net.nmoncho.sbt.dependencycheck.Utils.StringLogger
import org.mockito.Mockito._
import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS

class PurgeSuite extends munit.FunSuite {

  private val dataDirectory = Files.createTempDirectory(null)

  test("Purging an Engine should delegate to Engine.purge") {
    implicit val log: StringLogger = new StringLogger

    val settings = new Settings()
    settings.setString(KEYS.DATA_DIRECTORY, dataDirectory.toFile.getAbsolutePath)
    settings.setString(KEYS.DB_FILE_NAME, s"db-${UUID.randomUUID()}.db")

    val engine = mock(classOf[Engine])
    when(engine.getSettings).thenReturn(settings)
    when(engine.purge()).thenReturn(true)

    println(s"Purging on ${dataDirectory.toFile.getAbsolutePath}")
    Purge(engine)

    verify(engine, atMostOnce()).purge()
    assert(log.sb.result().contains("Cached web data sources purged successfully"))
  }

  test("Purging an Engine should try to delete the db if Engine.purge fails") {
    implicit val log: StringLogger = new StringLogger

    val settings = new Settings()
    settings.setString(KEYS.DATA_DIRECTORY, dataDirectory.toFile.getAbsolutePath)

    val tmpFile = Files.createTempFile(settings.getDataDirectory.toPath, null, null)
    settings.setString(KEYS.DB_FILE_NAME, tmpFile.toFile.getName)

    val engine = mock(classOf[Engine])
    when(engine.getSettings).thenReturn(settings)
    when(engine.purge()).thenReturn(false) // Engine.purge() fails

    assert(tmpFile.toFile.exists())

    println(s"Purging on ${dataDirectory.toFile.getAbsolutePath}")
    Purge(engine)

    verify(engine, atMostOnce()).purge()
    assert(log.sb.result().contains("Failed to purge cached web data sources"))
    assert(!tmpFile.toFile.exists(), "db file should be deleted as fallback")
  }

  test("Purging an Engine with an empty connection string should fail") {
    implicit val log: StringLogger = new StringLogger

    val settings = new Settings(new Properties())
    settings.setString(KEYS.DATA_DIRECTORY, dataDirectory.toFile.getAbsolutePath)

    val engine = mock(classOf[Engine])
    when(engine.getSettings).thenReturn(settings)

    println(s"Purging on ${dataDirectory.toFile.getAbsolutePath}")
    intercept[IllegalStateException] {
      Purge(engine)
    }

    verify(engine, never()).purge()
  }
}
