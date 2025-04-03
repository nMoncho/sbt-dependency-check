/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import sbt.util.Level
import sbt.util.Logger

object Utils {

  class StringLogger extends Logger {

    val sb: StringBuilder = new StringBuilder()

    override def trace(t: => Throwable): Unit = ()

    override def success(message: => String): Unit = sb.append(message).append('\n')

    override def log(level: Level.Value, message: => String): Unit = sb.append(message).append('\n')
  }

}
