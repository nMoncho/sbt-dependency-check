/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito._
import org.owasp.dependencycheck.Engine
import org.owasp.dependencycheck.analyzer.AbstractSuppressionAnalyzer.SUPPRESSION_OBJECT_KEY
import org.owasp.dependencycheck.analyzer.Analyzer
import org.owasp.dependencycheck.analyzer.VulnerabilitySuppressionAnalyzer
import org.owasp.dependencycheck.data.nexus.MavenArtifact
import org.owasp.dependencycheck.dependency.Dependency
import org.owasp.dependencycheck.dependency.naming.GenericIdentifier
import org.owasp.dependencycheck.dependency.naming.PurlIdentifier
import org.owasp.dependencycheck.xml.suppression.{ SuppressionRule => OwaspSuppressionRule }
import sbt._
import sbt.util.Logger

/** Offline unit tests for the engine-orchestration glue that is otherwise only reached through the
  * network-dependent scripted tests: suppression-rule injection, evidence/identifier attachment, and
  * the PURL-to-generic identifier fallback. Uses Mockito for the `Engine`/`Analyzer` (as in
  * `UpdateSuite`/`PurgeSuite`) and real `Dependency`/`MavenArtifact` objects.
  */
class EngineGlueSuite extends munit.FunSuite {

  private implicit val log: Logger = Logger.Null

  test("getIdentifier returns a PURL identifier for a well-formed artifact") {
    val moduleId = "org.example" % "widget" % "1.2.3"
    val artifact = new MavenArtifact("org.example", "widget", "1.2.3")

    val id = getIdentifier(artifact, moduleId)

    assert(id.isInstanceOf[PurlIdentifier], s"expected a PurlIdentifier but got [${id.getClass}]")
  }

  test("getIdentifier falls back to a generic identifier when the PURL is malformed") {
    val moduleId = "org.example" % "widget" % "1.2.3"
    // An empty artifact id makes the PackageURL name blank, so the PurlIdentifier constructor throws
    // and the code must fall back to a GenericIdentifier built from the module id.
    val artifact = new MavenArtifact("org.example", "", "1.2.3")

    val id = getIdentifier(artifact, moduleId)

    assert(id.isInstanceOf[GenericIdentifier], s"expected a GenericIdentifier but got [${id.getClass}]")
    assertEquals(id.asInstanceOf[GenericIdentifier].getValue, "sbt:org.example:widget:1.2.3")
  }

  test("addEvidence attaches a software identifier and evidence to the dependency") {
    val moduleId   = "org.example" % "widget" % "1.2.3"
    val dependency = new Dependency()

    addEvidence(moduleId, dependency)

    assert(!dependency.getSoftwareIdentifiers.isEmpty, "expected a software identifier to be added")
    assert(!dependency.getEvidence.isEmpty, "expected evidence to be added")
  }

  test("addSuppressionRules merges the rules into the engine's suppression list when enabled") {
    val analyzer = mock(classOf[VulnerabilitySuppressionAnalyzer])
    when(analyzer.isEnabled).thenReturn(true)

    val analyzers = new java.util.ArrayList[Analyzer]()
    analyzers.add(analyzer)

    // `getObject` is left unstubbed (returns null), so the code builds a fresh suppression list,
    // appends the project rules, and stores it via `putObject`, whose argument we capture.
    val engine = mock(classOf[Engine])
    when(engine.getAnalyzers()).thenReturn(analyzers)

    addSuppressionRules(Set(SuppressionRule(cvssBelow = Seq(10.0))), engine)

    val captor = ArgumentCaptor.forClass(classOf[java.util.List[OwaspSuppressionRule]])
    verify(engine).putObject(eqTo(SUPPRESSION_OBJECT_KEY), captor.capture())
    assertEquals(captor.getValue.size, 1, "the project's suppression rule should be stored on the engine")
  }

  test("addSuppressionRules does not touch the engine when the suppression analyzer is disabled") {
    val analyzer = mock(classOf[VulnerabilitySuppressionAnalyzer])
    when(analyzer.isEnabled).thenReturn(false)

    val analyzers = new java.util.ArrayList[Analyzer]()
    analyzers.add(analyzer)

    val engine = mock(classOf[Engine])
    when(engine.getAnalyzers()).thenReturn(analyzers)

    addSuppressionRules(Set(SuppressionRule(cvssBelow = Seq(10.0))), engine)

    verify(engine, never()).putObject(any(), any())
  }
}
