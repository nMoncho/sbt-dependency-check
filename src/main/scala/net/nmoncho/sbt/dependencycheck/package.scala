/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt

import java.io.PrintWriter
import java.io.StringWriter

import scala.util.Using

import org.owasp.dependencycheck.dependency.Vulnerability
import org.owasp.dependencycheck.exception.ExceptionCollection
import org.owasp.dependencycheck.utils.SeverityUtil
import sbt.Logger

package object dependencycheck {

  def logFailure(t: Throwable)(implicit log: Logger): Unit = t match {
    case e: VulnerabilityFoundException =>
      log.error(s"${e.getLocalizedMessage}")
      logThrowable(e)

    case e: ExceptionCollection =>
      import scala.jdk.CollectionConverters.*

      val prettyMessage = (
        "Dependency-Check failed with the following exceptions:" +:
          e.getExceptions.asScala.toVector.flatMap { t =>
            s"  ${t.getLocalizedMessage}" +:
            Option(t.getCause).map { cause =>
              s"  - ${cause.getLocalizedMessage}"
            }.toVector
          }
      ).mkString("\n")
      log.error(prettyMessage)

      logThrowable(e)

    case e =>
      log.error(s"Dependency-Check failed: ${e.getLocalizedMessage}")
      logThrowable(e)
  }

  def logThrowable(t: Throwable)(implicit log: Logger): Unit =
    // We have to log the full StackTraces here, since SBT doesn't use `printStackTrace`
    // when logging exceptions.
    Using.Manager { use =>
      val sw = use(new StringWriter)
      val pw = new PrintWriter(sw, true)

      t.printStackTrace(pw)
      log.error(sw.toString)
    }

  /** Checks if the given vulnerability is higher than the `failCvssScore`
    *
    * This will check against the different CVSS versions
    *
    * @param v             vulnerability to check
    * @param failCvssScore failing score
    * @return true if any score is higher than the failing score, false otherwise.
    */
  def failingVulnerability(v: Vulnerability, failCvssScore: Double): Boolean =
    (v.getCvssV2 != null && v.getCvssV2.getCvssData.getBaseScore >= failCvssScore) ||
      (v.getCvssV3 != null && v.getCvssV3.getCvssData.getBaseScore >= failCvssScore) ||
      (v.getCvssV4 != null && v.getCvssV4.getCvssData.getBaseScore >= failCvssScore) ||
      (v.getUnscoredSeverity != null && SeverityUtil.estimateCvssV2(
        v.getUnscoredSeverity
      ) >= failCvssScore) ||
      (failCvssScore <= 0.0f)

  /** The highest CVSS base score present on a vulnerability across CVSS v2, v3, and v4, or 0.0 when
    * none carries a numeric score (for example a vulnerability that only fails via the KEV flag or a
    * must-fail CVE id). Used to pick and label the most severe offending finding.
    */
  def vulnerabilityScore(v: Vulnerability): Double = {
    val scores = Seq(
      Option(v.getCvssV2).flatMap(c => Option(c.getCvssData.getBaseScore)),
      Option(v.getCvssV3).flatMap(c => Option(c.getCvssData.getBaseScore)),
      Option(v.getCvssV4).flatMap(c => Option(c.getCvssData.getBaseScore))
    ).flatten.map(_.doubleValue())

    if (scores.isEmpty) 0.0 else scores.max
  }

  /** Policy describing which vulnerabilities should fail the build.
    *
    * A vulnerability fails the build when it meets the CVSS threshold, or its id is in the must-fail
    * CVE list, or it is a Known Exploited Vulnerability and failing on those is enabled.
    *
    * @param failCvssScore CVSS score at or above which a vulnerability fails the build
    * @param failOnCves CVE ids that must always fail the build, regardless of their score
    * @param failOnKnownExploited whether any Known Exploited Vulnerability (KEV) should fail the build
    */
  final case class FailurePolicy(
      failCvssScore: Double,
      failOnCves: Set[String],
      failOnKnownExploited: Boolean
  ) {

    /** @return true if the given vulnerability should fail the build under this policy */
    def isFailing(v: Vulnerability): Boolean =
      failingVulnerability(v, failCvssScore) ||
        failOnCves.contains(v.getName) ||
        (failOnKnownExploited && v.getKnownExploitedVulnerability != null)
  }

  object FailurePolicy {

    /** A policy that gates the build on the CVSS threshold only (the historical behaviour). */
    def cvssOnly(failCvssScore: Double): FailurePolicy =
      FailurePolicy(failCvssScore, Set.empty, failOnKnownExploited = false)
  }
}
