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

import java.util.regex.Pattern

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin
import net.nmoncho.sbt.dependencycheck.Keys
import org.owasp.dependencycheck.utils.Settings
import sbt.Keys.streams
import sbt._

object ListSettings {

  private val autoImport: Keys.type = Keys

  import autoImport.*

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log

    val settings = DependencyCheckPlugin.engineSettings.value
    // Rebuild Masks for Masked Properties
    val masks = Option(settings.getArray(Settings.KEYS.MASKED_PROPERTIES))
      .getOrElse(Array.empty[String])
      .map(Pattern.compile(_).asPredicate())

    log.info(dependencyCheckScopes.value.toPrettyString().split('\n').mkString("\t", "\n\t", ""))

    keys().toSeq.sorted.foreach { key =>
      val value = settings.getString(key)

      if (value != null && masks.exists(_.test(key))) {
        log.info(s"\t$key: ********")
      } else {
        log.info(s"\t$key: $value")
      }

    }
  }

  /** Collect all [[Settings.KEYS]] values
    *
    * This method uses reflection due to the lack of methods to iterate over available keys
    *
    * @return All [[Settings.KEYS]] values
    */
  private def keys(): Set[String] = {
    val clazz = classOf[Settings.KEYS]

    clazz.getDeclaredFields.map { field =>
      field.get(clazz).asInstanceOf[String]
    }.toSet
  }
}
