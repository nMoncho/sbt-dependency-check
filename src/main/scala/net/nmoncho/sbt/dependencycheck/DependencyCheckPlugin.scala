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

import net.nmoncho.sbt.dependencycheck.settings._
import net.nmoncho.sbt.dependencycheck.tasks._
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import org.owasp.dependencycheck.utils.Settings
import sbt.AutoPlugin
import sbt.Compile
import sbt.Def
import sbt.File
import sbt.Global
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
    dependencyCheck := dependencyCheckTask.value,
    dependencyCheckAggregate := dependencyCheckAggregateTask.value,
    dependencyCheckAllProjects := dependencyCheckAllProjectsTask.value,
    dependencyCheckUpdate := dependencyCheckUpdateTask.value,
    dependencyCheckPurge := dependencyCheckPurgeTask.value,
    dependencyCheckListSettings := dependencyCheckListTask.value,
    dependencyCheckListUnusedSuppressions := dependencyCheckListUnusedTask.value,
    Compile / resourceGenerators += GenerateSuppressions.exportPackagedSuppressions(),
    dependencyCheckOutputDirectory := crossTarget.value,
    dependencyCheckAggregate / aggregate := false,
    dependencyCheckAllProjects / aggregate := false,
    dependencyCheckUpdate / aggregate := false,
    dependencyCheckPurge / aggregate := false,
    dependencyCheckListSettings / aggregate := false,
    Global / concurrentRestrictions += Tags.exclusive(NonParallel)
  )

  private def dependencyCheckTask: Def.Initialize[Task[Unit]] = Check()

  private def dependencyCheckAggregateTask: Def.Initialize[Task[Unit]] = AggregateCheck()

  private def dependencyCheckAllProjectsTask: Def.Initialize[Task[Unit]] = AllProjectsCheck()

  private def dependencyCheckUpdateTask: Def.Initialize[Task[Unit]] = Update()

  private def dependencyCheckPurgeTask: Def.Initialize[Task[Unit]] = Purge()

  private def dependencyCheckListTask: Def.Initialize[Task[Unit]] = ListSettings()

  private def dependencyCheckListUnusedTask: Def.Initialize[Task[Unit]] = ListUnusedSuppressions()

  lazy val engineSettings: Def.Initialize[Task[Settings]] = LoadSettings()

  lazy val scanSet: Def.Initialize[Task[Seq[File]]] = ScanSet()

}
