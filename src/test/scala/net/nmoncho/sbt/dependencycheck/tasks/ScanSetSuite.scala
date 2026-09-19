/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.nio.file.Files

import sbt._

/** Verifies that expanding the configured `dependencyCheckScanSet` handles an empty configuration as
  * a no-op instead of throwing `UnsupportedOperationException` from `reduceLeft` on an empty list.
  */
class ScanSetSuite extends munit.FunSuite {

  test("collect returns an empty result for an empty scan set instead of crashing") {
    assertEquals(ScanSet.collect(Seq.empty), Seq.empty[File])
  }

  test("collect expands scan-set directories into the files they contain") {
    val dir  = Files.createTempDirectory("scanset-test").toFile
    val file = dir / "resource.txt"
    IO.write(file, "content")

    try {
      assert(
        ScanSet.collect(Seq(dir)).contains(file),
        s"expected [$file] to be part of the collected scan set"
      )
    } finally {
      IO.delete(dir)
    }
  }
}
