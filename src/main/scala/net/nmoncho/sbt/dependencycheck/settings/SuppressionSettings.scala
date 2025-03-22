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

import org.owasp.dependencycheck.utils.Settings
import sbt.File

/** Suppression Settings
  *
  * Holds suppression as files or URLs, and hosted suppressions. The former
  * are project specific, whereas the latter are "base" suppression which can be
  * more general.
  *
  * @param files suppression files
  * @param hosted hosted suppressions
  */
case class SuppressionSettings(
    files: SuppressionFilesSettings,
    hosted: HostedSuppressionsSettings,
    generatedSuppressions: File,
    suppressions: Seq[SuppressionRule]
) {
  def apply(settings: Settings): Unit = {
    val filesUpdated = if (generatedSuppressions.exists()) {
      files.copy(files = files.files :+ generatedSuppressions.getAbsolutePath)
    } else {
      files
    }

    filesUpdated(settings)
    hosted(settings)
  }
}

object SuppressionSettings {

  val GeneratedSuppressionsFilename: String = "generated-suppressions-file.xml"

  val Default: SuppressionSettings = new SuppressionSettings(
    files                 = SuppressionFilesSettings.Default,
    hosted                = HostedSuppressionsSettings.Default,
    generatedSuppressions = new File(GeneratedSuppressionsFilename),
    suppressions          = Seq.empty
  )

  def apply(
      files: SuppressionFilesSettings    = SuppressionFilesSettings.Default,
      hosted: HostedSuppressionsSettings = HostedSuppressionsSettings.Default,
      generatedSuppressions: File        = new File(GeneratedSuppressionsFilename),
      suppressions: Seq[SuppressionRule] = Seq.empty
  ): SuppressionSettings =
    new SuppressionSettings(files, hosted, generatedSuppressions, suppressions)
}
