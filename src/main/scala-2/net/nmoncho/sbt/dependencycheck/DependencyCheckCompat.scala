/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import java.io.File

import scala.annotation.unused

import sbt.Configuration
import sbt.Def.Classpath
import sbt.ModuleID
import sbt.UpdateReport
import sbt.internal.util.Attributed
import xsbti.FileConverter

private[dependencycheck] object DependencyCheckCompat {

  def getModuleId[A](attributed: Attributed[A]): Option[ModuleID] =
    attributed.get(sbt.Keys.moduleID.key)

  def managedJars(
      config: Configuration,
      jarTypes: Set[String],
      updateReport: UpdateReport,
      @unused converter: FileConverter
  ): Classpath =
    sbt.Classpaths.managedJars(config, jarTypes, updateReport)

  def classpathToFiles(
      classpath: Classpath,
      @unused converter: FileConverter
  ): Seq[Attributed[File]] = classpath

}
