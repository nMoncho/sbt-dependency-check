/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import sbt.complete.Parser

/** Offline tests for the `dependencyCheck` command-surface parsers. They map CLI tokens to
  * [[ParseOptions]] / [[ProjectSelection]] and are otherwise only exercised by a handful of concrete
  * scripted invocations (the three summary args are exercised nowhere else). `Parser.parse` needs no
  * engine or network and is stable across the sbt 1.x / 2.x cross-build.
  */
class ArgumentParserSuite extends munit.FunSuite {

  private def parseArgs(input: String): Either[String, Seq[ParseOptions]] =
    Parser.parse(input, Check.argumentsParser)

  test("each long-form argument maps to its ParseOptions") {
    assertEquals(parseArgs(" --list-settings"), Right(Seq(ParseOptions.ListSettings)))
    assertEquals(parseArgs(" --single-report"), Right(Seq(ParseOptions.SingleReport)))
    assertEquals(parseArgs(" --all-projects"), Right(Seq(ParseOptions.AllProjects)))
    assertEquals(
      parseArgs(" --list-unused-suppressions"),
      Right(Seq(ParseOptions.ListUnusedSuppressions))
    )
    assertEquals(parseArgs(" original-summary"), Right(Seq(ParseOptions.OriginalSummary)))
    assertEquals(
      parseArgs(" all-vulnerabilities-summary"),
      Right(Seq(ParseOptions.AllVulnerabilitiesSummary))
    )
    assertEquals(
      parseArgs(" offending-vulnerabilities-summary"),
      Right(Seq(ParseOptions.OffendingVulnerabilitiesSummary))
    )
  }

  test("each short-form flag maps to the same ParseOptions as its long form") {
    assertEquals(parseArgs(" -l"), Right(Seq(ParseOptions.ListSettings)))
    assertEquals(parseArgs(" -s"), Right(Seq(ParseOptions.SingleReport)))
    assertEquals(parseArgs(" -a"), Right(Seq(ParseOptions.AllProjects)))
    assertEquals(parseArgs(" -u"), Right(Seq(ParseOptions.ListUnusedSuppressions)))
  }

  test("no arguments parse to an empty sequence") {
    assertEquals(parseArgs(""), Right(Seq.empty[ParseOptions]))
  }

  test("arguments are accepted in any order and combination") {
    assertEquals(
      parseArgs(" --single-report --all-projects"),
      Right(Seq(ParseOptions.SingleReport, ParseOptions.AllProjects))
    )
    assertEquals(
      parseArgs(" --all-projects --single-report"),
      Right(Seq(ParseOptions.AllProjects, ParseOptions.SingleReport))
    )
    // Long and short forms mix freely, and a bare summary arg combines with the flags.
    assertEquals(
      parseArgs(" -l offending-vulnerabilities-summary"),
      Right(Seq(ParseOptions.ListSettings, ParseOptions.OffendingVulnerabilitiesSummary))
    )
  }

  test("the same argument may be repeated") {
    assertEquals(
      parseArgs(" --list-settings --list-settings"),
      Right(Seq(ParseOptions.ListSettings, ParseOptions.ListSettings))
    )
  }

  test("an unrecognized token fails to parse") {
    assert(parseArgs(" bogus").isLeft, "an unknown argument should not parse")
    assert(
      parseArgs(" --list-settings bogus").isLeft,
      "a trailing unknown argument should not parse"
    )
  }

  test("projectSelectionParser maps the selection tokens and defaults to none") {
    assertEquals(
      Parser.parse(" --per-project", projectSelectionParser),
      Right(Some(ProjectSelection.PerProject))
    )
    assertEquals(
      Parser.parse(" -p", projectSelectionParser),
      Right(Some(ProjectSelection.PerProject))
    )
    assertEquals(
      Parser.parse(" --all-projects", projectSelectionParser),
      Right(Some(ProjectSelection.AllProjects))
    )
    assertEquals(
      Parser.parse(" -a", projectSelectionParser),
      Right(Some(ProjectSelection.AllProjects))
    )
    assertEquals(
      Parser.parse(" --aggregate", projectSelectionParser),
      Right(Some(ProjectSelection.Aggregate))
    )
    assertEquals(
      Parser.parse(" -g", projectSelectionParser),
      Right(Some(ProjectSelection.Aggregate))
    )
    assertEquals(Parser.parse("", projectSelectionParser), Right(None))
  }
}
