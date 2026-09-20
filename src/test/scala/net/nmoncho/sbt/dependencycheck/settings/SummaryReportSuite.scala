/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import io.github.jeremylong.openvulnerability.client.nvd.CvssV2
import io.github.jeremylong.openvulnerability.client.nvd.CvssV2Data
import io.github.jeremylong.openvulnerability.client.nvd.CvssV3
import io.github.jeremylong.openvulnerability.client.nvd.CvssV3Data
import net.nmoncho.sbt.dependencycheck.FailurePolicy
import org.mockito.Mockito._
import org.owasp.dependencycheck.dependency.Dependency
import org.owasp.dependencycheck.dependency.Vulnerability

/** Tests the three [[SummaryReport]] strategies, which build the human-readable summary printed when
  * a build fails. These lock in the score formatting (CVSSv2/v3/unscored) and, crucially, the
  * distinction between `AllVulnerabilities` (every finding) and `OffendingVulnerabilities` (only
  * findings that fail the policy), which is the same `isFailing` predicate that gates the build.
  */
class SummaryReportSuite extends munit.FunSuite {

  private def vulnV3(name: String, score: Double): Vulnerability = {
    val data = mock(classOf[CvssV3Data])
    when(data.getBaseScore).thenReturn(java.lang.Double.valueOf(score))
    val cvss = mock(classOf[CvssV3])
    when(cvss.getCvssData).thenReturn(data)

    val v = new Vulnerability(name)
    v.setCvssV3(cvss)
    v
  }

  private def vulnV2(name: String, score: Double): Vulnerability = {
    val data = mock(classOf[CvssV2Data])
    when(data.getBaseScore).thenReturn(java.lang.Double.valueOf(score))
    val cvss = mock(classOf[CvssV2])
    when(cvss.getCvssData).thenReturn(data)

    val v = new Vulnerability(name)
    v.setCvssV2(cvss)
    v
  }

  private def vulnUnscored(name: String, severity: String): Vulnerability = {
    val v = new Vulnerability(name)
    v.setUnscoredSeverity(severity)
    v
  }

  private def dependency(fileName: String, vulns: Vulnerability*): Dependency = {
    val d = new Dependency()
    d.setFileName(fileName)
    vulns.foreach(d.addVulnerability)
    d
  }

  private val policy = FailurePolicy.cvssOnly(7.0)

  test("AllVulnerabilities includes every vulnerability with its score, offending or not") {
    val deps = Seq(
      dependency("offending.jar", vulnV3("CVE-OFFENDING", 9.1)),
      dependency("clean.jar", vulnV3("CVE-CLEAN", 3.0))
    )

    val summary = SummaryReport.AllVulnerabilities.buildSummary(deps, policy)

    assert(summary.contains("CVE-OFFENDING"), summary)
    assert(summary.contains("CVE-CLEAN"), summary)
    assert(summary.contains("CVSSv3 9.1"), summary)
    assert(summary.contains("CVSSv3 3.0"), summary)
    assert(summary.contains("offending.jar"), summary)
    assert(summary.contains("clean.jar"), summary)
  }

  test("OffendingVulnerabilities includes only vulnerabilities that fail the policy") {
    val deps = Seq(
      dependency("offending.jar", vulnV3("CVE-OFFENDING", 9.1)),
      dependency("clean.jar", vulnV3("CVE-CLEAN", 3.0))
    )

    val summary = SummaryReport.OffendingVulnerabilities.buildSummary(deps, policy)

    assert(summary.contains("CVE-OFFENDING"), summary)
    assert(
      !summary.contains("CVE-CLEAN"),
      s"below-threshold finding should be filtered out: $summary"
    )
    // A dependency with no offending finding is omitted entirely.
    assert(!summary.contains("clean.jar"), s"clean dependency should not appear: $summary")
  }

  test("processVulnerability renders v2, v3, and unscored score labels") {
    val deps = Seq(
      dependency(
        "mixed.jar",
        vulnV2("CVE-V2", 8.0),
        vulnV3("CVE-V3", 9.1),
        vulnUnscored("CVE-UNSCORED", "HIGH")
      )
    )

    val summary = SummaryReport.AllVulnerabilities.buildSummary(deps, policy)

    assert(summary.contains("CVSSv2 8.0"), summary)
    assert(summary.contains("CVSSv3 9.1"), summary)
    assert(summary.contains("Unscored (estimated)"), summary)
  }

  test("Original lists each dependency's vulnerability ids") {
    val deps = Seq(dependency("offending.jar", vulnV3("CVE-OFFENDING", 9.1)))

    val summary = SummaryReport.Original.buildSummary(deps, policy)

    assert(summary.contains("offending.jar"), summary)
    assert(summary.contains("CVE-OFFENDING"), summary)
  }

  test("a summary of dependencies without vulnerabilities is empty") {
    val summary =
      SummaryReport.AllVulnerabilities.buildSummary(Seq(dependency("clean.jar")), policy)

    assert(summary.isEmpty, s"expected an empty summary but got: $summary")
  }
}
