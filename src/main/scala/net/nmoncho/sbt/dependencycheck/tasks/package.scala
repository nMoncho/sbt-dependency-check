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

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.control.NonFatal

import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.agent.DependencyCheckScanAgent
import org.owasp.dependencycheck.analyzer.AbstractSuppressionAnalyzer.SUPPRESSION_OBJECT_KEY
import org.owasp.dependencycheck.analyzer.VulnerabilitySuppressionAnalyzer
import org.owasp.dependencycheck.data.nexus.MavenArtifact
import org.owasp.dependencycheck.dependency.Confidence
import org.owasp.dependencycheck.dependency.Dependency
import org.owasp.dependencycheck.dependency.EvidenceType
import org.owasp.dependencycheck.dependency.naming.GenericIdentifier
import org.owasp.dependencycheck.dependency.naming.Identifier
import org.owasp.dependencycheck.dependency.naming.PurlIdentifier
import org.owasp.dependencycheck.reporting.ReportGenerator.Format
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS.APPLICATION_NAME
import org.owasp.dependencycheck.utils.SeverityUtil
import org.owasp.dependencycheck.xml.suppression.{ SuppressionRule => OwaspSuppressionRule }
import sbt.Tags.Tag
import sbt.{ Keys => SbtKeys, _ }

package object tasks {

  val NonParallel: Tag = Tags.Tag("NonParallel")

  def withEngine[A](settings: Settings)(fn: Engine => A)(implicit log: Logger): A = {
    val oldClassLoader = Thread.currentThread().getContextClassLoader
    val newClassLoader = classOf[Engine].getClassLoader
    val engine         = new Engine(newClassLoader, settings)

    try {
      Thread.currentThread().setContextClassLoader(newClassLoader)

      try {
        fn(engine)
      } catch {
        case NonFatal(e) =>
          logFailure(e)
          throw e
      }

    } finally {
      engine.close()
      engine.getSettings.cleanup(true)
      Thread.currentThread().setContextClassLoader(oldClassLoader)
    }
  }

  def logAddDependencies(
      classpath: Seq[Attributed[File]],
      configuration: Configuration
  )(implicit log: Logger): Seq[Attributed[File]] =
    logDependencies(classpath, configuration, "Adding")

  def logRemoveDependencies(
      classpath: Seq[Attributed[File]],
      configuration: Configuration
  )(implicit log: Logger): Seq[Attributed[File]] =
    logDependencies(classpath, configuration, "Removing")

  def logDependencies(
      classpath: Seq[Attributed[File]],
      configuration: Configuration,
      action: String
  )(implicit log: Logger): Seq[Attributed[File]] = {
    log.debug(s"$action ${configuration.name} dependencies to check.")
    classpath.foreach(f => log.debug("\t" + f.data.getName))
    classpath
  }

  def analyzeProject(
      projectName: String,
      engine: Engine,
      dependencies: Set[Attributed[File]],
      suppressionRules: Seq[SuppressionRule],
      scanSet: Seq[File],
      failCvssScore: Double,
      outputDir: File,
      reportFormats: Seq[Format]
  )(implicit log: Logger): Unit = {
    addSuppressionRules(suppressionRules, engine)
    addDependencies(dependencies, engine)
    scanSet.foreach(file => engine.scan(file))

    engine.analyzeDependencies()

    if (reportFormats.isEmpty) {
      log.info("No Report Format was selected for the Dependency Check Analysis")
    }

    reportFormats.foreach(reportFormat =>
      engine.writeReports(
        engine.getSettings.getString(APPLICATION_NAME),
        outputDir,
        reportFormat.name(),
        null
      )
    )

    failOnFoundVulnerabilities(failCvssScore, engine, projectName)
  }

  private def addSuppressionRules(rules: Seq[SuppressionRule], engine: Engine): Unit = {
    import scala.jdk.CollectionConverters.*

    engine.getAnalyzers().asScala.foreach {
      case analyzer: VulnerabilitySuppressionAnalyzer =>
        // We have to prepare the analyzer first before adding any other suppression rules
        // This way the XML and Hosted Suppressions are loaded first, then the ones defined
        // in the project being analyzed (i.e. in the `build.sbt` or imported as packaged suppressions)
        analyzer.prepare(engine)
        if (analyzer.isEnabled) {
          val engineRules = Option(engine.getObject(SUPPRESSION_OBJECT_KEY))
            .map(_.asInstanceOf[java.util.List[OwaspSuppressionRule]])
            .getOrElse(new java.util.ArrayList[OwaspSuppressionRule]())

          engineRules.addAll(rules.map(_.toOwasp).asJavaCollection)
          engine.putObject(SUPPRESSION_OBJECT_KEY, engineRules)
        }

      case _ =>
    }
  }

  private def addDependencies(
      checkClasspath: Set[Attributed[File]],
      engine: Engine
  )(implicit log: Logger): Unit =
    checkClasspath.foreach(attributed =>
      if (attributed.data != null) {
        val dependencies = engine.scan(new File(attributed.data.getAbsolutePath))

        // Add evidence if is managed dependency, otherwise just scan the file
        for {
          moduleId <- attributed.get(SbtKeys.moduleID.key)
          nonEmptyDependencies <- Option(dependencies).filterNot(_.isEmpty)
          dependency <- Option(nonEmptyDependencies.get(0))
        } yield addEvidence(moduleId, dependency)
      } else {
        // I don't think this can be `null`, but lifting it from previous plugin
        log.warn(s"Attributed[File] = [$attributed] has null data and won't be scanned")
      }
    )

  private def addEvidence(
      moduleId: ModuleID,
      dependency: Dependency
  ): Unit = {
    val artifact = new MavenArtifact(moduleId.organization, moduleId.name, moduleId.revision)
    dependency.addAsEvidence("sbt", artifact, Confidence.HIGHEST)

    val id = getIdentifier(artifact, moduleId)
    dependency.addSoftwareIdentifier(id)

    moduleId.configurations.foreach(configurations =>
      dependency.addEvidence(
        EvidenceType.VENDOR,
        "sbt",
        "configuration",
        configurations,
        Confidence.HIGHEST
      )
    )
  }

  private def getIdentifier(artifact: MavenArtifact, moduleId: ModuleID): Identifier =
    Try {
      new PurlIdentifier(
        "sbt",
        artifact.getGroupId,
        artifact.getArtifactId,
        artifact.getVersion,
        Confidence.HIGHEST
      )
    } match {
      case Success(id) => id
      case Failure(_) =>
        new GenericIdentifier(
          String.format("sbt:%s:%s:%s", moduleId.organization, moduleId.name, moduleId.revision),
          Confidence.HIGHEST
        )
    }

  private def failOnFoundVulnerabilities(
      failCvssScore: Double,
      engine: Engine,
      name: String
  ): Unit = {
    import scala.jdk.CollectionConverters.*

    val hasFailingVulnerabilities = engine.getDependencies.exists { p =>
      p.getVulnerabilities.asScala.exists { v =>
        (v.getCvssV2 != null && v.getCvssV2.getCvssData.getBaseScore >= failCvssScore) ||
        (v.getCvssV3 != null && v.getCvssV3.getCvssData.getBaseScore >= failCvssScore) ||
        (v.getUnscoredSeverity != null && SeverityUtil.estimateCvssV2(
          v.getUnscoredSeverity
        ) >= failCvssScore) ||
        (failCvssScore <= 0.0f)
      }
    }

    if (hasFailingVulnerabilities) {
      DependencyCheckScanAgent.showSummary(name, engine.getDependencies)

      throw new VulnerabilityFoundException(
        s"Vulnerability with CVSS score higher than [$failCvssScore] found"
      )
    }
  }

}
