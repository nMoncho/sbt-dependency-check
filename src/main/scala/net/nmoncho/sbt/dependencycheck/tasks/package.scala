/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.control.NonFatal

import net.nmoncho.sbt.dependencycheck.settings.SummaryReport
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import org.owasp.dependencycheck.Engine
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
import org.owasp.dependencycheck.utils.Downloader
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.xml.suppression.{ SuppressionRule => OwaspSuppressionRule }
import sbt.Tags.Tag
import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete.Parser
import xsbti.FileConverter

package object tasks {

  private[tasks] sealed abstract class ParseResult extends Product with Serializable

  private[tasks] sealed abstract class ProjectSelection extends ParseResult
  private[tasks] object ProjectSelection {
    case object PerProject extends ProjectSelection
    case object AllProjects extends ProjectSelection
    case object Aggregate extends ProjectSelection
  }

  private[tasks] sealed abstract class ParseOptions extends ParseResult
  private[tasks] object ParseOptions {
    case object ListSettings extends ParseOptions
    case object SingleReport extends ParseOptions
    case object AllProjects extends ParseOptions
    case object ListUnusedSuppressions extends ParseOptions

    case object OriginalSummary extends ParseOptions
    case object AllVulnerabilitiesSummary extends ParseOptions
    case object OffendingVulnerabilitiesSummary extends ParseOptions
  }

  private[tasks] val PerProject  = (Space ~> token("per-project")) ^^^ ProjectSelection.PerProject
  private[tasks] val AllProjects = (Space ~> token("all-projects")) ^^^ ProjectSelection.AllProjects
  private[tasks] val Aggregate   = (Space ~> token("aggregate")) ^^^ ProjectSelection.Aggregate

  private[tasks] val ListSettingsArg =
    (Space ~> token("list-settings")) ^^^ ParseOptions.ListSettings
  private[tasks] val SingleReportArg =
    (Space ~> token("single-report")) ^^^ ParseOptions.SingleReport
  private[tasks] val AllProjectsArg =
    (Space ~> token("all-projects")) ^^^ ParseOptions.AllProjects
  private[tasks] val ListUnusedSuppressionsArg =
    (Space ~> token("list-unused-suppressions")) ^^^ ParseOptions.ListUnusedSuppressions

  private[tasks] val OriginalSummaryArg =
    (Space ~> token("original-summary")) ^^^ ParseOptions.OriginalSummary
  private[tasks] val AllVulnerabilitiesSummaryArg =
    (Space ~> token("all-vulnerabilities-summary")) ^^^ ParseOptions.AllVulnerabilitiesSummary
  private[tasks] val OffendingVulnerabilitiesSummaryArg =
    (Space ~> token(
      "offending-vulnerabilities-summary"
    )) ^^^ ParseOptions.OffendingVulnerabilitiesSummary

  private[tasks] val projectSelectionParser: Parser[Option[ParseResult]] =
    (PerProject | AllProjects | Aggregate).?

  val NonParallel: Tag = Tags.Tag("NonParallel")

  def withEngine[A](settings: Settings)(fn: Engine => A)(implicit log: Logger): A = {
    val oldClassLoader = Thread.currentThread().getContextClassLoader
    val newClassLoader = classOf[Engine].getClassLoader
    val engine         = new Engine(newClassLoader, settings)

    try {
      Thread.currentThread().setContextClassLoader(newClassLoader)

      try {
        Downloader.getInstance().configure(settings)
        fn(engine)
      } catch {
        case NonFatal(e) =>
          logFailure(e)
          throw e
      }

    } finally {
      cleanupEngine(engine, oldClassLoader)
    }
  }

  /** Closes the engine, cleans up its settings, and restores the thread context classloader.
    *
    * Each step is guarded independently so a failure in one does not skip the others. In particular
    * the classloader restore must always run: the context classloader is swapped on a reused sbt
    * worker thread, so a skipped restore could corrupt later tasks. Guarding also prevents a
    * secondary cleanup failure from masking the primary exception being propagated out of
    * [[withEngine]]'s `try`.
    */
  private[tasks] def cleanupEngine(engine: Engine, oldClassLoader: ClassLoader)(
      implicit log: Logger
  ): Unit = {
    try engine.close()
    catch {
      case NonFatal(t) =>
        log.warn(s"Failed to close the dependency-check engine: ${t.getMessage}")
    }

    try engine.getSettings.cleanup(true)
    catch {
      case NonFatal(t) =>
        log.warn(s"Failed to clean up the dependency-check settings: ${t.getMessage}")
    }

    Thread.currentThread().setContextClassLoader(oldClassLoader)
  }

  def logAddDependencies(
      classpath: sbt.Def.Classpath,
      configuration: Configuration
  )(implicit log: Logger, converter: FileConverter): Seq[Attributed[File]] = {
    import sbtcompat.PluginCompat.*

    logDependencies(classpath.map(_.map(toFile)), configuration, "Adding")
  }

  def logRemoveDependencies(
      classpath: sbt.Def.Classpath,
      configuration: Configuration
  )(implicit log: Logger, converter: FileConverter): Seq[Attributed[File]] = {
    import sbtcompat.PluginCompat.*

    logDependencies(classpath.map(_.map(toFile)), configuration, "Removing")
  }

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
      suppressionRules: Set[SuppressionRule],
      scanSet: Seq[File],
      failurePolicy: FailurePolicy,
      warnOnly: Boolean,
      outputDir: File,
      reportFormats: Seq[Format],
      summaryReport: SummaryReport
  )(implicit log: Logger): Unit = {
    runAnalysis(engine, dependencies, suppressionRules, scanSet)

    if (reportFormats.isEmpty) {
      log.info("No Report Format was selected for the Dependency Check Analysis")
    } else {
      log.info(s"Writing Dependency Check reports to [${outputDir.getAbsolutePath}]")
      reportFormats.foreach(reportFormat =>
        engine.writeReports(
          projectName,
          outputDir,
          reportFormat.name(),
          null
        )
      )
    }

    failOnFoundVulnerabilities(failurePolicy, warnOnly, engine, projectName, summaryReport)
  }

  /** Adds suppression rules and dependencies to the engine, scans the scan set, and runs the OWASP
    * analysis. Shared by [[analyzeProject]] and the suppression-baseline task.
    */
  private[tasks] def runAnalysis(
      engine: Engine,
      dependencies: Set[Attributed[File]],
      suppressionRules: Set[SuppressionRule],
      scanSet: Seq[File]
  )(implicit log: Logger): Unit = {
    addSuppressionRules(suppressionRules, engine)
    addDependencies(dependencies, engine)
    scanSet.foreach(file => engine.scan(file))

    engine.analyzeDependencies()
  }

  private def addSuppressionRules(rules: Set[SuppressionRule], engine: Engine)(
      implicit log: Logger
  ): Unit = {
    import scala.jdk.CollectionConverters.*

    log.info(s"Adding [${rules.size}] suppression rules to Owasp Engine")
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
        import sbtcompat.PluginCompat._

        val dependencies = engine.scan(new File(attributed.data.getAbsolutePath))

        // Add evidence if is managed dependency, otherwise just scan the file
        for {
          moduleId <- attributed.get(moduleIDStr).map(parseModuleIDStrAttribute)
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
      failurePolicy: FailurePolicy,
      warnOnly: Boolean,
      engine: Engine,
      name: String,
      summaryReport: SummaryReport
  )(implicit log: Logger): Unit = {
    import scala.jdk.CollectionConverters.*

    val hasFailingVulnerabilities = engine.getDependencies.exists { p =>
      p.getVulnerabilities.asScala.exists(failurePolicy.isFailing)
    }

    if (hasFailingVulnerabilities) {
      SummaryReport.showSummary(name, engine.getDependencies, failurePolicy, summaryReport)

      if (warnOnly) {
        log.warn(
          s"Vulnerabilities failing the configured policy were found in [$name], but 'dependencyCheckWarnOnly' is enabled so the build will not fail."
        )
      } else {
        throw new VulnerabilityFoundException(
          s"Vulnerability failing the configured policy found (CVSS threshold [${failurePolicy.failCvssScore}])"
        )
      }
    }
  }

}
