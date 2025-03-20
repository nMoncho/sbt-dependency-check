package net.nmoncho.sbt.dependencycheck.settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS.*
import sbt.{ File, URL }

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
