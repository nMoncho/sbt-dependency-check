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

package net.nmoncho.sbt.dependencycheck

import org.owasp.dependencycheck.utils.Settings
import sbt._

package object settings {
  trait SettingSetter[A] {
    def set(setting: Settings, key: String, value: A): Unit
  }

  implicit val StringSetter: SettingSetter[String] =
    (setting: Settings, key: String, value: String) => setting.setString(key, value)
  implicit val IntSetter: SettingSetter[Int] =
    (setting: Settings, key: String, value: Int) => setting.setInt(key, value)
  implicit val LongSetter: SettingSetter[Long] =
    (setting: Settings, key: String, value: Long) => setting.setString(key, value.toString)
  implicit val BooleanSetter: SettingSetter[Boolean] =
    (setting: Settings, key: String, value: Boolean) => setting.setBoolean(key, value)
  implicit val FileSetter: SettingSetter[File] =
    (setting: Settings, key: String, value: File) => setting.setString(key, value.getAbsolutePath)
  implicit val UrlSetter: SettingSetter[URL] =
    (setting: Settings, key: String, value: URL) => setting.setString(key, value.toExternalForm)
  implicit val SeqStringSetter: SettingSetter[Seq[String]] = {
    // 'setArrayIfNotEmpty' could be problematic if an empty array has meaning for the key being set
    (setting: Settings, key: String, value: Seq[String]) =>
      setting.setArrayIfNotEmpty(key, value.toArray)
  }

  implicit def OptionSetter[A](implicit inner: SettingSetter[A]): SettingSetter[Option[A]] =
    (setting: Settings, key: String, value: Option[A]) => value.foreach(setting.set(key, _))

  implicit class SettingsOps(private val settings: Settings) extends AnyVal {

    def set[A: SettingSetter](key: String, value: A): Unit =
      implicitly[SettingSetter[A]].set(settings, key, value)

  }
}
