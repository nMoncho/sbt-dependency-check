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

/** Database Settings
  *
  * Database where vulnerabilities are stored for the analysis.
  *
  * @param driverName the database driver class name. An embedded database is used by default
  * @param driverPath the database driver class path
  * @param connectionString the database connection string
  * @param username username to use when connecting to the database
  * @param password password to use when connecting to the database
  * @param dataDirectory base path to use for the data directory (for embedded db and other cached resources from the Internet)
  * @param batchInsertEnabled adds capabilities to batch insert. Tested on PostgreSQL and H2
  * @param batchInsertSize Size of database batch inserts
  */
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
