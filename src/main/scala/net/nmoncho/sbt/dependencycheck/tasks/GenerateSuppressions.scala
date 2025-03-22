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

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckSuppressions
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import sbt.Def
import sbt.File
import sbt.IO
import sbt.Keys.streams
import sbt.Logger
import sbt.Task

/** Generates the XML Suppression File containing suppressions specified
  * using [[SuppressionRule]]s
  */
object GenerateSuppressions {

  def apply(): Def.Initialize[Task[File]] = Def.taskDyn {
    implicit val log: Logger = streams.value.log
    val settings             = dependencyCheckSuppressions.value

    if (settings.suppressions.nonEmpty) {
      log.info(
        s"Generating suppression file to [${settings.generatedSuppressions.getAbsolutePath}]"
      )

      Def.task {
        IO.write(
          settings.generatedSuppressions,
          SuppressionRule.toSuppressionsXML(settings.suppressions)
        )

        settings.generatedSuppressions
      }
    } else {
      log.debug("No suppressions defined, skipping suppression file generation...")
      Def.task(settings.generatedSuppressions)
    }
  }

}
