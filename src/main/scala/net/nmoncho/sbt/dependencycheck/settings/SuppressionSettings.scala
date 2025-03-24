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

import scala.util.matching.Regex

import org.owasp.dependencycheck.utils.Settings
import sbt.File
import sbt.Keys
import sbt.internal.util.Attributed

/** Suppression Settings
  *
  * Holds suppression as files or URLs, and hosted suppressions. The former
  * are project specific, whereas the latter are "base" suppression which can be
  * more general.
  *
  * @param files suppression files
  * @param hosted hosted suppressions
  * @param suppressions suppressions defined in the project definition (e.g. a `build.sbt`)
  * @param packagedEnabled whether the packaged suppressions rules are enabled
  * @param packagedFilter which dependencies should be considered when importing packaged suppression rules
  */
case class SuppressionSettings(
    files: SuppressionFilesSettings,
    hosted: HostedSuppressionsSettings,
    suppressions: Seq[SuppressionRule],
    packagedEnabled: Boolean,
    packagedFilter: SuppressionSettings.PackagedFilter
) {

  def apply(settings: Settings): Unit = {
    files(settings)
    hosted(settings)
  }
}

object SuppressionSettings {

  final val DefaultPackagedFilter: PackagedFilter = PackagedFilter.BlacklistAll

  final val PackagedSuppressionsFilename: String = "packaged-suppressions-file.xml"

  final val Default: SuppressionSettings = new SuppressionSettings(
    files           = SuppressionFilesSettings.Default,
    hosted          = HostedSuppressionsSettings.Default,
    suppressions    = Seq.empty,
    packagedEnabled = false,
    packagedFilter  = DefaultPackagedFilter
  )

  def apply(
      files: SuppressionFilesSettings    = Default.files,
      hosted: HostedSuppressionsSettings = Default.hosted,
      suppressions: Seq[SuppressionRule] = Default.suppressions,
      packagedEnabled: Boolean           = Default.packagedEnabled,
      packagedFilter: PackagedFilter     = Default.packagedFilter
  ): SuppressionSettings =
    new SuppressionSettings(
      files,
      hosted,
      suppressions,
      packagedEnabled,
      packagedFilter
    )

  type PackagedFilter = Attributed[File] => Boolean

  object PackagedFilter {

    final val BlacklistAll: PackagedFilter = _ => false

    final val WhitelistAll: PackagedFilter = _ => true

    /** Filter dependencies based on their GAV identifiers
      *
      * @param pred a function that takes a GAV (GroudId, ArtifactId, Version) return true if it should consider that artifact.
      */
    def ofGav(pred: (String, String, String) => Boolean): PackagedFilter =
      (dependency: Attributed[File]) => {
        dependency.get(Keys.moduleID.key).exists(m => pred(m.organization, m.name, m.revision))
      }

    /** Filter dependencies based on a file check
      */
    def ofFile(pred: File => Boolean): PackagedFilter =
      (dependency: Attributed[File]) => pred(dependency.data)

    /** Filter dependencies based on a filename check
      */
    def ofFilename(pred: String => Boolean): PackagedFilter =
      (dependency: Attributed[File]) => pred(dependency.data.getName)

    /** Filter dependencies based on a filename check against a Regex
      */
    def ofFilenameRegex(regex: Regex): PackagedFilter =
      (dependency: Attributed[File]) => regex.findFirstMatchIn(dependency.data.getName).nonEmpty
  }
}
