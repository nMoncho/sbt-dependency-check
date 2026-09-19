/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.net.URL
import java.net.URLClassLoader

import net.nmoncho.sbt.dependencycheck.Utils.StringLogger
import org.mockito.Mockito._
import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.utils.Settings

/** Tests for [[cleanupEngine]]: even if `engine.close()` fails, the settings must still be cleaned
  * up and the thread context classloader must still be restored (it is swapped on a reused sbt
  * worker thread), and the secondary failure must not propagate.
  */
class CleanupEngineSuite extends munit.FunSuite {

  test("a failing engine.close() does not skip settings cleanup or the classloader restore") {
    implicit val log: StringLogger = new StringLogger

    val settings = mock(classOf[Settings])
    val engine   = mock(classOf[Engine])
    doThrow(new RuntimeException("close failed")).when(engine).close()
    when(engine.getSettings).thenReturn(settings)

    val original = Thread.currentThread().getContextClassLoader
    val swapped  = new URLClassLoader(Array.empty[URL], original)

    try {
      Thread.currentThread().setContextClassLoader(swapped)

      // Must not throw, despite close() failing.
      cleanupEngine(engine, original)

      assertEquals(
        Thread.currentThread().getContextClassLoader,
        original,
        "the context classloader must be restored even when close() throws"
      )
      verify(engine).close()
      verify(settings).cleanup(true) // cleanup still runs after a close() failure
      assert(log.sb.result().contains("Failed to close"), "the close failure is surfaced as a warning")
    } finally {
      Thread.currentThread().setContextClassLoader(original)
    }
  }
}
