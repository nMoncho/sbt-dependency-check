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
