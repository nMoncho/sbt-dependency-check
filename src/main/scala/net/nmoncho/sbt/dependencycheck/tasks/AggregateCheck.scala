package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.{ engineSettings, scanSet }
import net.nmoncho.sbt.dependencycheck.Keys.*
import sbt.*
import sbt.Keys.*

object AggregateCheck {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log
    log.info(s"Running aggregate check for [${name.value}]")

    val failCvssScore = dependencyCheckFailBuildOnCVSS.value
    val scanSetFiles  = scanSet.value

    val dependencies = scala.collection.mutable.Set[Attributed[File]]()
    dependencies ++= logAddDependencies(aggregateCompileFilter.value.flatten, Compile)
    dependencies ++= logAddDependencies(aggregateTestFilter.value.flatten, Test)
    dependencies ++= logAddDependencies(aggregateRuntimeFilter.value.flatten, Runtime)
    dependencies --= logRemoveDependencies(aggregateProvidedFilter.value.flatten, Provided)
    dependencies --= logRemoveDependencies(aggregateOptionalFilter.value.flatten, Optional)

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

  private lazy val aggregateCompileFilter = Def.settingDyn {
    compileDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Compile))
    )
  }

  private lazy val aggregateRuntimeFilter = Def.settingDyn {
    runtimeDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Runtime))
    )
  }

  private lazy val aggregateTestFilter = Def.settingDyn {
    testDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Test))
    )
  }

  private lazy val aggregateProvidedFilter = Def.settingDyn {
    providedDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Provided))
    )
  }

  private lazy val aggregateOptionalFilter = Def.settingDyn {
    optionalDependenciesTask.all(
      ScopeFilter(inAggregates(thisProjectRef.value), inConfigurations(Optional))
    )
  }
}
