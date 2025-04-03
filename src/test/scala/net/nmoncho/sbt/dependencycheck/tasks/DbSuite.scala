/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import scala.util.Properties.envOrNone

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.util.Logger

class DbSuite extends munit.FunSuite {

  override def munitTimeout: Duration = new FiniteDuration(30, TimeUnit.MINUTES)

  test("Pull DB") {
    for {
      _ <- envOrNone("CI").filter(_.toBoolean)
      folder <- envOrNone("DATA_DIRECTORY")
      nvdApiKey <- envOrNone("NVD_API_KEY")
    } yield {
      val settings = new Settings()
      settings.setStringIfNotEmpty(DATA_DIRECTORY, folder)
      settings.setStringIfNotEmpty(NVD_API_KEY, nvdApiKey)

      withEngine(settings) { engine =>
        engine.analyzeDependencies()
      }(Logger.Null)
    }
  }

}
