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

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.scanSet
import net.nmoncho.sbt.dependencycheck.Keys._
import net.nmoncho.sbt.dependencycheck.tasks.Dependencies._
import sbt.Keys._
import sbt._

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
