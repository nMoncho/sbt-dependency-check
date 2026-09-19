/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

/** Database Settings
  *
  * Database where vulnerabilities are stored for the analysis.
  *
  * The data directory (base path for the embedded DB and cached resources) is configured with the
  * top-level `dependencyCheckDataDirectory` setting, which writes the same OWASP `DATA_DIRECTORY`
  * key.
  *
  * @param driverName the database driver class name. An embedded database is used by default
  * @param driverPath the database driver class path
  * @param connectionString the database connection string
  * @param username username to use when connecting to the database
  * @param password password to use when connecting to the database
  * @param batchInsertEnabled adds capabilities to batch insert. Tested on PostgreSQL and H2
  * @param batchInsertSize Size of database batch inserts
  */
case class DatabaseSettings(
    driverName: Option[String]       = None,
    driverPath: Option[String]       = None,
    connectionString: Option[String] = None,
    username: Option[String]         = None,
    @redacted
    password: Option[String]            = None,
    batchInsertEnabled: Option[Boolean] = None,
    batchInsertSize: Option[Int]        = None
) {

  def configure(settings: Settings): Unit = {
    settings.set(DB_DRIVER_NAME, driverName)
    settings.set(DB_DRIVER_PATH, driverPath)
    settings.set(DB_CONNECTION_STRING, connectionString)
    settings.set(DB_USER, username)
    settings.set(DB_PASSWORD, password)

    settings.set(ENABLE_BATCH_UPDATES, batchInsertEnabled)
    settings.set(MAX_BATCH_SIZE, batchInsertSize)
  }

  override def toString: String = redactedToString(this)
}

object DatabaseSettings {
  val Default: DatabaseSettings = DatabaseSettings()
}
