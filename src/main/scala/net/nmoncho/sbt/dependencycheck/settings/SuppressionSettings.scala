/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import scala.util.matching.Regex

import net.nmoncho.sbt.dependencycheck.DependencyCheckCompat
import org.owasp.dependencycheck.utils.Settings
import sbt.File
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
        DependencyCheckCompat
          .getModuleId(dependency)
          .exists(m => pred(m.organization, m.name, m.revision))
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
