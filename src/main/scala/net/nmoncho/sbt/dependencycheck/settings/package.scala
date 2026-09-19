/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import java.lang.reflect.Modifier

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

  /** Renders a case class similarly to its compiler-generated `toString`, but replaces the fields
    * annotated with `@redacted` with a redacted placeholder. Used by the secret-bearing settings
    * classes so `show <settingKey>` (and any diagnostic logging of them) never prints credentials in
    * cleartext.
    *
    * Which fields are secret lives at the field declaration via the `@redacted` annotation rather
    * than in a separate list here, so it cannot drift out of sync when fields are added or reordered.
    * `@redacted` is a `RUNTIME`-retained Java annotation so it is readable through reflection on both
    * the Scala 2.12 and Scala 3 cross-builds. Where scalac emits it differs between versions (2.12
    * places a `@Target(FIELD)` Java annotation on the primary-constructor parameter of a case-class
    * `val`, not on the backing field), so a field is treated as secret if either the field itself or
    * the constructor parameter at the same position carries the annotation. An empty or `None` secret
    * is shown as-is so it is clear nothing was configured.
    */
  private[settings] def redactedToString(product: Product): String = {
    val parameterAnnotations =
      product.getClass.getDeclaredConstructors
        .maxBy(_.getParameterCount)
        .getParameterAnnotations

    def parameterIsRedacted(index: Int): Boolean =
      index < parameterAnnotations.length &&
        parameterAnnotations(index).exists(_.isInstanceOf[redacted])

    product.getClass.getDeclaredFields.iterator
      .filterNot(field => field.isSynthetic || Modifier.isStatic(field.getModifiers))
      .zipWithIndex
      .map { case (field, index) =>
        field.setAccessible(true)
        val secret   = field.isAnnotationPresent(classOf[redacted]) || parameterIsRedacted(index)
        val rendered =
          if (secret) redactSecretValue(field.get(product))
          else String.valueOf(field.get(product))

        s"${field.getName}=$rendered"
      }
      .mkString(s"${product.productPrefix}(", ", ", ")")
  }

  private def redactSecretValue(value: Any): String = value match {
    case None => "None"
    case Some(_) => "Some(********)"
    case null => "null"
    case s: String if s.isEmpty => ""
    case _ => "********"
  }
}
