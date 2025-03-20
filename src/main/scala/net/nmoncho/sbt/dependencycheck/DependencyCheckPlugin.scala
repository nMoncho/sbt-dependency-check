package net.nmoncho.sbt.dependencycheck

import net.nmoncho.sbt.dependencycheck.settings.*
import net.nmoncho.sbt.dependencycheck.tasks.*
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import org.owasp.dependencycheck.utils.Settings
import sbt.Keys.*
import sbt.{ Def, * }
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
    dependencyCheckAnalyzer := AnalyzerSettings.Default,
    dependencyCheckSuppressionFiles := SuppressionFilesSettings.Default,
    dependencyCheckScopes := ScopesSettings.Default,
    dependencyCheckDatabase := DatabaseSettings.Default,
    dependencyCheckNvdApi := NvdApiSettings.Default,
    dependencyCheckProxy := ProxySettings.Default,
    dependencyCheckHostedSuppressions := HostedSuppressionsSettings.Default,
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
    dependencyCheckOutputDirectory := crossTarget.value,
    dependencyCheckAggregate / aggregate := false,
    dependencyCheckAllProjects / aggregate := false,
    dependencyCheckUpdate / aggregate := false,
    dependencyCheckPurge / aggregate := false,
    dependencyCheckListSettings / aggregate := false
  )

  private def dependencyCheckTask: Def.Initialize[Task[Unit]] = Check()

  private def dependencyCheckAggregateTask: Def.Initialize[Task[Unit]] = AggregateCheck()

  private def dependencyCheckAllProjectsTask: Def.Initialize[Task[Unit]] = AllProjectsCheck()

  private def dependencyCheckUpdateTask: Def.Initialize[Task[Unit]] = Update()

  private def dependencyCheckPurgeTask: Def.Initialize[Task[Unit]] = Purge()

  private def dependencyCheckListTask: Def.Initialize[Task[Unit]] = ListSettings()

  lazy val engineSettings: Def.Initialize[Task[Settings]] = LoadSettings()

  lazy val scanSet: Def.Initialize[Task[Seq[File]]] = ScanSet()

}
