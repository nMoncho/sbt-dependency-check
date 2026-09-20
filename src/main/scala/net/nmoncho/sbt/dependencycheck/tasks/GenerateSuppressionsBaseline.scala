/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package tasks

import scala.jdk.CollectionConverters._

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.scanSet
import net.nmoncho.sbt.dependencycheck.Keys._
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import org.owasp.dependencycheck.dependency.Dependency
import sbt.Keys._
import sbt._

/** Task that runs the analysis and writes a suppression XML baseline covering the vulnerabilities
  * currently found. A team adopting the plugin on an existing codebase can use it to baseline known
  * findings and then fail only on new ones.
  */
object GenerateSuppressionsBaseline {

  private[tasks] final val OutputFileName = "dependency-check-suppressions.xml"

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log

    val settings     = engineSettings.value
    val dependencies = Dependencies.projectDependencies.value
    val suppressions = GenerateSuppressions.forProject.value
    val scan         = scanSet.value
    val outputFile   = dependencyCheckOutputDirectory.value / OutputFileName
    val projectName  = name.value

    withEngine(settings) { engine =>
      runAnalysis(engine, dependencies, suppressions, scan)

      val rules = rulesFrom(engine.getDependencies.toSeq)

      if (rules.isEmpty) {
        log.info(
          s"No vulnerabilities found for [$projectName]; no suppression baseline was written."
        )
      } else {
        IO.write(outputFile, SuppressionRule.toSuppressionsXML(rules))
        log.info(
          s"Wrote a suppression baseline with [${rules.size}] rule(s) to [${outputFile.getAbsolutePath}]"
        )
      }
    }
  } tag NonParallel

  /** Derives one suppression rule per vulnerable dependency, listing the CVEs currently found.
    *
    * Each rule targets its dependency by Package URL when one is available (stable and readable),
    * otherwise by SHA1, otherwise by file path. Dependencies without vulnerabilities are skipped.
    * Output is ordered by file name so the generated baseline is stable across runs.
    */
  private[tasks] def rulesFrom(dependencies: Seq[Dependency]): Seq[SuppressionRule] =
    dependencies
      .sortBy(dependency => Option(dependency.getFileName).getOrElse(""))
      .flatMap { dependency =>
        val cves = dependency.getVulnerabilities.asScala.toSeq
          .map(_.getName)
          .filter(name => name != null && name.nonEmpty)
          .distinct
          .sorted

        if (cves.isEmpty) {
          None
        } else {
          val notes = s"Baselined from ${dependency.getFileName}"

          val packageUrl = dependency.getSoftwareIdentifiers.asScala.toSeq
            .map(_.getValue)
            .find(value => value != null && value.startsWith("pkg:"))

          packageUrl
            .map(purl => SuppressionRule.ofPackageUrl(value = purl, cve = cves, notes = notes))
            .orElse(
              Option(dependency.getSha1sum)
                .filter(_.nonEmpty)
                .map(sha1 => SuppressionRule.ofSha1(value = sha1, cve = cves, notes = notes))
            )
            .orElse(
              Option(dependency.getFilePath)
                .filter(_.nonEmpty)
                .map(path => SuppressionRule.ofFilePath(value = path, cve = cves, notes = notes))
            )
        }
      }
}
