/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule
import org.owasp.dependencycheck.dependency.Confidence
import org.owasp.dependencycheck.dependency.Dependency
import org.owasp.dependencycheck.dependency.Vulnerability
import org.owasp.dependencycheck.dependency.naming.GenericIdentifier

class GenerateSuppressionsBaselineSuite extends munit.FunSuite {

  private def dependency(
      fileName: String,
      filePath: String           = "",
      sha1: String               = "",
      packageUrl: Option[String] = None,
      cves: Seq[String]
  ): Dependency = {
    val dependency = new Dependency()
    dependency.setFileName(fileName)
    if (filePath.nonEmpty) dependency.setFilePath(filePath)
    if (sha1.nonEmpty) dependency.setSha1sum(sha1)
    packageUrl.foreach(purl =>
      dependency.addSoftwareIdentifier(new GenericIdentifier(purl, Confidence.HIGHEST))
    )
    cves.foreach(cve => dependency.addVulnerability(new Vulnerability(cve)))
    dependency
  }

  private def xmlFor(dependency: Dependency): String =
    SuppressionRule.toSuppressionsXML(GenerateSuppressionsBaseline.rulesFrom(Seq(dependency)))

  test("targets a dependency by Package URL when one is available") {
    val xml = xmlFor(
      dependency(
        fileName   = "foo-1.0.jar",
        sha1       = "abc123",
        packageUrl = Some("pkg:maven/org.example/foo@1.0"),
        cves       = Seq("CVE-2021-1111")
      )
    )

    assert(xml.contains("<packageUrl"), s"expected a packageUrl rule in:\n$xml")
    assert(xml.contains("pkg:maven/org.example/foo@1.0"), xml)
    assert(xml.contains("<cve>CVE-2021-1111</cve>"), xml)
    assert(!xml.contains("<sha1>"), "packageUrl must be preferred over sha1")
  }

  test("falls back to SHA1 when no Package URL is present") {
    val xml = xmlFor(
      dependency(fileName = "bar-2.0.jar", sha1 = "def456", cves = Seq("CVE-2020-2222"))
    )

    assert(xml.contains("<sha1>def456</sha1>"), xml)
    assert(xml.contains("<cve>CVE-2020-2222</cve>"), xml)
  }

  test("falls back to file path when neither Package URL nor SHA1 is present") {
    val xml = xmlFor(
      dependency(
        fileName = "baz.jar",
        filePath = "/repo/baz.jar",
        cves     = Seq("CVE-2019-3333")
      )
    )

    assert(xml.contains("<filePath"), xml)
    assert(xml.contains("/repo/baz.jar"), xml)
  }

  test("skips dependencies without vulnerabilities") {
    val rules = GenerateSuppressionsBaseline.rulesFrom(
      Seq(
        dependency(fileName = "clean.jar", sha1      = "aaa", cves = Seq.empty),
        dependency(fileName = "vulnerable.jar", sha1 = "bbb", cves = Seq("CVE-2022-4444"))
      )
    )

    assertEquals(rules.size, 1, "only the vulnerable dependency should yield a rule")
  }

  test("de-duplicates and sorts the CVE list") {
    val xml = xmlFor(
      dependency(
        fileName = "dup.jar",
        sha1     = "ccc",
        cves     = Seq("CVE-2021-2222", "CVE-2021-1111", "CVE-2021-1111")
      )
    )

    val first  = xml.indexOf("CVE-2021-1111")
    val second = xml.indexOf("CVE-2021-2222")
    assert(first >= 0 && second >= 0, xml)
    assert(first < second, s"CVEs should be sorted:\n$xml")

    val occurrences = "CVE-2021-1111".r.findAllMatchIn(xml).size
    assertEquals(occurrences, 1, s"CVE-2021-1111 should appear once:\n$xml")
  }
}
