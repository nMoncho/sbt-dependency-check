package net.nmoncho.sbt.dependencycheck

import org.owasp.dependencycheck.utils.Settings
import sbt.*

package object settings {
  trait SettingSetter[A] {
    def set(setting: Settings, key: String, value: A): Unit
  }

  implicit val StringSetter: SettingSetter[String] =
    (setting: Settings, key: String, value: String) => setting.setString(key, value)
  implicit val IntSetter: SettingSetter[Int] =
    (setting: Settings, key: String, value: Int) => setting.setInt(key, value)
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
