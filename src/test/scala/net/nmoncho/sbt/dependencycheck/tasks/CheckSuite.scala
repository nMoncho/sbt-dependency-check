/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

/** Tests for [[Check.selectProjectMode]], the mapping from parsed command arguments to the analysis
  * mode. Previously the dispatch had an unreachable `else` guard and silently ran the per-project
  * path when `all-projects` was passed without `single-report`.
  */
class CheckSuite extends munit.FunSuite {

  test(
    "all-projects always selects the combined all-projects report, with or without single-report"
  ) {
    assertEquals(
      Check.selectProjectMode(allProjects = true, singleReport = false),
      ProjectSelection.AllProjects
    )
    assertEquals(
      Check.selectProjectMode(allProjects = true, singleReport = true),
      ProjectSelection.AllProjects
    )
  }

  test("single-report on its own selects the aggregate report") {
    assertEquals(
      Check.selectProjectMode(allProjects = false, singleReport = true),
      ProjectSelection.Aggregate
    )
  }

  test("no arguments selects the per-project report") {
    assertEquals(
      Check.selectProjectMode(allProjects = false, singleReport = false),
      ProjectSelection.PerProject
    )
  }
}
