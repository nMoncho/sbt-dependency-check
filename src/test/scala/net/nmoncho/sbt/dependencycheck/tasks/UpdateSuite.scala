/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Utils.StringLogger
import org.mockito.Mockito._
import org.owasp.dependencycheck.Engine
import sbt.Logger

class UpdateSuite extends munit.FunSuite {

  test("The Update task should delegate update to the Owasp Engine") {
    implicit val log: Logger = Logger.Null

    val engine = mock(classOf[Engine])

    Update(engine)

    verify(engine, atLeastOnce()).doUpdates()
  }

  test("The Update task should report failures when delegating update to the Owasp Engine") {
    implicit val log: StringLogger = new StringLogger

    val engine = mock(classOf[Engine])
    when(engine.doUpdates()).thenThrow(new IllegalStateException("Some expected error"))

    intercept[IllegalStateException] {
      Update(engine)
    }

    verify(engine, atLeastOnce()).doUpdates()

    val output = log.sb.result()
    assert(output.contains("Failed to update the NVD data"), s"accurate NVD-update label:\n$output")
    assert(output.contains("dependencyCheckNvdApi"), "actionable NVD API key hint is present")
    assert(
      !output.contains("connecting to the local database"),
      "a remote update failure must not be blamed on the local database"
    )
  }

}
