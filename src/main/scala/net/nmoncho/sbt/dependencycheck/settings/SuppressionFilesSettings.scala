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

package net.nmoncho.sbt.dependencycheck.settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.File
import sbt.URL

/** Suppression Files used to ignore false positives.
  *
  * @param files files or urls to consider
  * @param user he username used when connecting to the suppressionFiles. For use when your suppressionFiles are hosted on a site requiring HTTP-Basic-authentication.
  * @param password the password used when connecting to the suppressionFiles. For use when your suppressionFiles are hosted on a site requiring HTTP-Basic-authentication.
  * @param bearerToken the token used when connecting to the suppressionFiles. For use when your suppressionFiles are hosted on a site requiring HTTP-Bearer-authentication.
  */
case class SuppressionFilesSettings(
    files: Seq[String],
    user: Option[String],
    password: Option[String],
    bearerToken: Option[String]
) {

  def apply(settings: Settings): Unit = {
    settings.set(SUPPRESSION_FILE, files)
    settings.set(SUPPRESSION_FILE_USER, user)
    settings.set(SUPPRESSION_FILE_PASSWORD, password)
    settings.set(SUPPRESSION_FILE_BEARER_TOKEN, bearerToken)
  }

}

object SuppressionFilesSettings {
  val Default: SuppressionFilesSettings = new SuppressionFilesSettings(Seq.empty, None, None, None)

  def apply(
      user: Option[String]        = None,
      password: Option[String]    = None,
      bearerToken: Option[String] = None
  )(files: File*)(urls: URL*): SuppressionFilesSettings =
    new SuppressionFilesSettings(
      files.map(_.getAbsolutePath) ++ urls.map(_.toExternalForm),
      user,
      password,
      bearerToken
    )

  def files(
      user: Option[String]        = None,
      password: Option[String]    = None,
      bearerToken: Option[String] = None
  )(files: File*): SuppressionFilesSettings =
    new SuppressionFilesSettings(
      files.map(_.getAbsolutePath),
      user,
      password,
      bearerToken
    )

  def urls(
      user: Option[String]        = None,
      password: Option[String]    = None,
      bearerToken: Option[String] = None
  )(urls: URL*): SuppressionFilesSettings =
    new SuppressionFilesSettings(
      urls.map(_.toExternalForm),
      user,
      password,
      bearerToken
    )
}
