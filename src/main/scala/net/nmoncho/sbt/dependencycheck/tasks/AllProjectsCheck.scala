package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.{ engineSettings, scanSet }
import net.nmoncho.sbt.dependencycheck.Keys.*
import sbt.*
import sbt.Keys.*

object AllProjectsCheck {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log
    log.info(s"Running AllProjects check for [${name.value}]")

    val failCvssScore           = dependencyCheckFailBuildOnCVSS.value
    val scanSetFiles: Seq[File] = scanSet.value

    val dependencies = scala.collection.mutable.Set[Attributed[File]]()
    dependencies ++= logAddDependencies(anyCompileFilter.value.flatten, Compile)
    dependencies --= logRemoveDependencies(anyProvidedFilter.value.flatten, Provided)
    dependencies ++= logAddDependencies(anyRuntimeFilter.value.flatten, Runtime)
    dependencies ++= logAddDependencies(anyTestFilter.value.flatten, Test)
    dependencies --= logRemoveDependencies(anyOptionalFilter.value.flatten, Optional)

    log.info("Scanning following dependencies: ")
    dependencies.foreach(f => log.info("\t" + f.data.getName))

    withEngine(engineSettings.value) { engine =>
      analyzeProject(
        name.value,
        engine,
        dependencies.toSet,
        scanSetFiles,
        failCvssScore,
        dependencyCheckOutputDirectory.value,
        dependencyCheckFormats.value
      )
    }
  }

  private lazy val anyCompileFilter = Def.settingDyn {
    compileDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Compile)))
  }
  private lazy val anyRuntimeFilter = Def.settingDyn {
    runtimeDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Runtime)))
  }
  private lazy val anyTestFilter = Def.settingDyn {
    testDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Test)))
  }
  private lazy val anyProvidedFilter = Def.settingDyn {
    providedDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Provided)))
  }
  private lazy val anyOptionalFilter = Def.settingDyn {
    optionalDependenciesTask.all(ScopeFilter(inAnyProject, inConfigurations(Optional)))
  }
}
