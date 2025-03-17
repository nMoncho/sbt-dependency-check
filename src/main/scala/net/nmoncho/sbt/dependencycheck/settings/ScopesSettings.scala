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
    compile: Boolean,
    test: Boolean,
    runtime: Boolean,
    provided: Boolean,
    optional: Boolean
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

  final val Default: ScopesSettings = new ScopesSettings(
    compile  = true,
    test     = false,
    runtime  = true,
    provided = true,
    optional = true
  )

  def apply(
      compile: Boolean  = true,
      test: Boolean     = false,
      runtime: Boolean  = true,
      provided: Boolean = true,
      optional: Boolean = true
  ): ScopesSettings =
    new ScopesSettings(compile, test, runtime, provided, optional)
}
