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
