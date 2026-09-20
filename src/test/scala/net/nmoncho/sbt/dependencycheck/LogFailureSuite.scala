/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import net.nmoncho.sbt.dependencycheck.Utils.StringLogger

/** Tests for [[logFailure]]: a non-report failure (scan, configuration, or NVD update) must not be
  * mislabeled as a report-creation error.
  */
class LogFailureSuite extends munit.FunSuite {

  test("a generic failure is not labeled as a report-creation error") {
    implicit val log: StringLogger = new StringLogger

    logFailure(new RuntimeException("scan blew up"))

    val output = log.sb.result()
    assert(output.contains("Dependency-Check failed: scan blew up"), output)
    assert(
      !output.contains("Failed creating report"),
      "a generic failure must not be mislabeled as report creation"
    )
  }
}
