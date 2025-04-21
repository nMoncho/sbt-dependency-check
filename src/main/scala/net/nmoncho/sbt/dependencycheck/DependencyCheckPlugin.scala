/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import net.nmoncho.sbt.dependencycheck.settings._
import net.nmoncho.sbt.dependencycheck.tasks._
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import org.owasp.dependencycheck.utils.Settings
import sbt.AutoPlugin
import sbt.Compile
import sbt.Def
import sbt.File
import sbt.Global
import sbt.InputTask
import sbt.Keys._
import sbt.PluginTrigger
import sbt.Tags
import sbt.Task
import sbt.io.syntax._
import sbt.plugins.JvmPlugin

object DependencyCheckPlugin extends AutoPlugin {

  override def requires = JvmPlugin

  override def trigger: PluginTrigger = allRequirements

  val autoImport: Keys.type = Keys

  import autoImport.*

  override def globalSettings: Seq[Def.Setting[?]] = Seq(
    dependencyCheckAutoUpdate := true,
    dependencyCheckSettingsFile := new File("dependencycheck.properties"),
    dependencyCheckFailBuildOnCVSS := 11.0,
    dependencyCheckJUnitFailBuildOnCVSS := None,
    dependencyCheckFormats := List(Format.HTML),
    dependencyCheckAnalysisTimeout := None,
    dependencyCheckDataDirectory := None,
    dependencyCheckAnalyzers := AnalyzerSettings.Default,
    dependencyCheckSuppressions := SuppressionSettings.Default,
    dependencyCheckScopes := ScopesSettings.Default,
    dependencyCheckDatabase := DatabaseSettings.Default,
    dependencyCheckNvdApi := NvdApiSettings.Default,
    dependencyCheckProxy := ProxySettings.Default,
    dependencyCheckConnectionTimeout := None,
    dependencyCheckConnectionReadTimeout := None
  )

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    dependencyCheckSkip := false,
    dependencyCheckScanSet := List(baseDirectory.value / "src" / "main" / "resources"),
    dependencyCheck := dependencyCheckTask.evaluated,
    dependencyCheckAggregate := dependencyCheckAggregateTask.value,
    dependencyCheckAllProjects := dependencyCheckAllProjectsTask.value,
    dependencyCheckUpdate := dependencyCheckUpdateTask.value,
    dependencyCheckPurge := dependencyCheckPurgeTask.value,
    dependencyCheckListSettings := dependencyCheckListTask.value,
    dependencyCheckListUnusedSuppressions := dependencyCheckListUnusedTask.value,
    dependencyCheckListSuppressions := ListSuppressions().evaluated,
    Compile / resourceGenerators += GenerateSuppressions.exportPackagedSuppressions(),
    dependencyCheckOutputDirectory := crossTarget.value,
    dependencyCheckAggregate / aggregate := false,
    dependencyCheckAllProjects / aggregate := false,
    dependencyCheckUpdate / aggregate := false,
    dependencyCheckPurge / aggregate := false,
    dependencyCheckListSettings / aggregate := false,
    dependencyCheckListSuppressions / aggregate := false,
    Global / concurrentRestrictions += Tags.exclusive(NonParallel)
  )

  private def dependencyCheckTask: Def.Initialize[InputTask[Unit]] = Check()

  private def dependencyCheckAggregateTask: Def.Initialize[Task[Unit]] = AggregateCheck()

  private def dependencyCheckAllProjectsTask: Def.Initialize[Task[Unit]] = AllProjectsCheck()

  private def dependencyCheckUpdateTask: Def.Initialize[Task[Unit]] = Update()

  private def dependencyCheckPurgeTask: Def.Initialize[Task[Unit]] = Purge()

  private def dependencyCheckListTask: Def.Initialize[Task[Unit]] = ListSettings()

  private def dependencyCheckListUnusedTask: Def.Initialize[Task[Unit]] = ListUnusedSuppressions()

  lazy val engineSettings: Def.Initialize[Task[Settings]] = LoadSettings()

  lazy val scanSet: Def.Initialize[Task[Seq[File]]] = ScanSet()

}
