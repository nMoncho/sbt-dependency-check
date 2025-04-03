/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt

import java.io.PrintWriter
import java.io.StringWriter

import scala.util.Using

import org.owasp.dependencycheck.exception.ExceptionCollection
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
}
