package net.nmoncho.sbt.dependencycheck.settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS.*
import sbt.File

case class DatabaseSettings(
    driverName: Option[String],
    driverPath: Option[String],
    connectionString: Option[String],
    username: Option[String],
    password: Option[String],
    dataDirectory: Option[File],
    batchInsertEnabled: Option[Boolean],
    batchInsertSize: Option[Int]
) {

  def apply(settings: Settings): Unit = {
    settings.set(DB_DRIVER_NAME, driverName)
    settings.set(DB_DRIVER_PATH, driverPath)
    settings.set(DB_CONNECTION_STRING, connectionString)
    settings.set(DB_USER, username)
    settings.set(DB_PASSWORD, password)
    settings.set(DATA_DIRECTORY, dataDirectory)

    settings.set(ENABLE_BATCH_UPDATES, batchInsertEnabled)
    settings.set(MAX_BATCH_SIZE, batchInsertSize)
  }
}

object DatabaseSettings {
  val Default: DatabaseSettings =
    new DatabaseSettings(None, None, None, None, None, None, None, None)

  def apply(
      driverName: Option[String]          = None,
      driverPath: Option[String]          = None,
      connectionString: Option[String]    = None,
      username: Option[String]            = None,
      password: Option[String]            = None,
      dataDirectory: Option[File]         = None,
      batchInsertEnabled: Option[Boolean] = None,
      batchInsertSize: Option[Int]        = None
  ): DatabaseSettings =
    new DatabaseSettings(
      driverName,
      driverPath,
      connectionString,
      username,
      password,
      dataDirectory,
      batchInsertEnabled,
      batchInsertSize
    )
}
