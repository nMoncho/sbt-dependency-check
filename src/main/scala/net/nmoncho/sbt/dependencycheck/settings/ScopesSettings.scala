/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

/** Defines what Library Dependency Scopes are included in the analysis
  *
  * @param compile whether Compile dependencies should be considered
  * @param test whether Test dependencies should be considered
  * @param runtime whether Runtime dependencies should be considered
  * @param provided whether Provided dependencies should be considered
  * @param optional whether Optional dependencies should be considered
  */
case class ScopesSettings(
    compile: Boolean  = true,
    test: Boolean     = false,
    runtime: Boolean  = true,
    provided: Boolean = true,
    optional: Boolean = true
) {

  def toPrettyString(): String =
    s"""ScopesSettings:
       |  compile: $compile
       |  test: $test
       |  runtime: $runtime
       |  provided: $provided
       |  optional: $optional
       |""".stripMargin

}

object ScopesSettings {

  final val Default: ScopesSettings = ScopesSettings()
}
