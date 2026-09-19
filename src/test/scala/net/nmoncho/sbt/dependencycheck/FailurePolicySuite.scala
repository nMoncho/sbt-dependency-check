/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import io.github.jeremylong.openvulnerability.client.nvd.CvssV3
import io.github.jeremylong.openvulnerability.client.nvd.CvssV3Data
import org.mockito.Mockito._
import org.owasp.dependencycheck.data.knownexploited.json.{
  Vulnerability => KnownExploitedVulnerability
}
import org.owasp.dependencycheck.dependency.Vulnerability

/** Tests for [[FailurePolicy]], which extends build gating beyond the single CVSS threshold with a
  * must-fail CVE list and a fail-on-Known-Exploited-Vulnerability flag.
  */
class FailurePolicySuite extends munit.FunSuite {

  private def vuln(
      name: String            = "CVE-0000-0000",
      cvssV3: Option[Double]  = None,
      knownExploited: Boolean = false
  ): Vulnerability = {
    val v = mock(classOf[Vulnerability])
    when(v.getName).thenReturn(name)
    cvssV3.foreach { score =>
      val data = mock(classOf[CvssV3Data])
      when(data.getBaseScore).thenReturn(java.lang.Double.valueOf(score))
      val cvss = mock(classOf[CvssV3])
      when(cvss.getCvssData).thenReturn(data)
      when(v.getCvssV3).thenReturn(cvss)
    }
    if (knownExploited) {
      when(v.getKnownExploitedVulnerability).thenReturn(mock(classOf[KnownExploitedVulnerability]))
    }
    v
  }

  test("fails on a CVE in the must-fail list regardless of score") {
    val policy = FailurePolicy(
      failCvssScore        = 11.0,
      failOnCves           = Set("CVE-2021-44228"),
      failOnKnownExploited = false
    )

    assert(policy.isFailing(vuln(name = "CVE-2021-44228")), "listed CVE must fail")
    assert(
      !policy.isFailing(vuln(name = "CVE-2000-1111")),
      "unlisted CVE with no score must not fail at the 11.0 threshold"
    )
  }

  test("fails on a Known Exploited Vulnerability only when the flag is enabled") {
    val enabled  = FailurePolicy(11.0, Set.empty, failOnKnownExploited = true)
    val disabled = FailurePolicy(11.0, Set.empty, failOnKnownExploited = false)

    assert(enabled.isFailing(vuln(knownExploited = true)), "KEV must fail when enabled")
    assert(!disabled.isFailing(vuln(knownExploited = true)), "KEV must not fail when disabled")
    assert(
      !enabled.isFailing(vuln(knownExploited = false)),
      "non-KEV must not fail on the KEV rule"
    )
  }

  test("still gates on the CVSS threshold") {
    val strict = FailurePolicy(7.0, Set.empty, failOnKnownExploited = false)
    assert(strict.isFailing(vuln(cvssV3 = Some(9.0))), "9.0 >= 7.0 must fail")

    val lenient = FailurePolicy(11.0, Set.empty, failOnKnownExploited = false)
    assert(!lenient.isFailing(vuln(cvssV3 = Some(9.0))), "9.0 < 11.0 must not fail")
  }

  test("cvssOnly builds a threshold-only policy") {
    val policy = FailurePolicy.cvssOnly(7.0)

    assertEquals(policy.failOnCves, Set.empty[String])
    assertEquals(policy.failOnKnownExploited, false)
    assert(policy.isFailing(vuln(cvssV3 = Some(8.0))), "8.0 >= 7.0 must fail")
  }
}
