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
        "Failed creating report:" +:
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
      log.error(s"Failed creating report: ${e.getLocalizedMessage}")
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
      (v.getUnscoredSeverity != null && SeverityUtil.estimateCvssV2(
        v.getUnscoredSeverity
      ) >= failCvssScore) ||
      (failCvssScore <= 0.0f)
}
