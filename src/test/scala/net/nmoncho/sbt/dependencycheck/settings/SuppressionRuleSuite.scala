/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.time.LocalDate

import scala.math.BigDecimal.RoundingMode

import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule.Identifier
import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule.PropertyType
import org.owasp.dependencycheck.xml.suppression.SuppressionParser
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop._
import org.scalacheck.Test
import sbt.Logger

class SuppressionRuleSuite extends munit.ScalaCheckSuite {

  override def scalaCheckTestParameters: Test.Parameters =
    Test.Parameters.default.withMinSuccessfulTests(100)

  property("Suppression rules generate valid XML") {
    forAll(Gen.nonEmptyListOf(genRule)) { rules =>
      val today  = LocalDate.now()
      val xml    = SuppressionRule.toSuppressionsXML(rules)
      val parser = new SuppressionParser

      if (rules.nonEmpty) {
        try {
          parser
            .parseSuppressionRules(
              new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")))
            )
            .size() == rules.count(r => r.until == LocalDate.EPOCH || r.until.compareTo(today) > 0)
        } catch {
          case t: Throwable =>
            println(xml)
            println("-------------------------")
            throw t
        }

      } else {
        true
      }
    }
  }

  property("Suppression rules can round-trip between Owasp and Plugin representations") {
    implicit val log: Logger = Logger.Null
    forAll(genRule) { rule =>
      val other = SuppressionRule.fromOwasp(rule.toOwasp)
      assertEquals(other.base, rule.base)
      assertEquals(other.until, rule.until)
      assertEquals(other.identifier, rule.identifier)
      assertEquals(other.cpe, rule.cpe)
      assertEquals(other.cvssBelow, rule.cvssBelow)
      assertEquals(other.cwe, rule.cwe)
      assertEquals(other.cve, rule.cve)
      assertEquals(other.vulnerabilityNames, rule.vulnerabilityNames)
      assertEquals(other.notes, rule.notes)
    }
  }

  private val genFilePath: Gen[Identifier] = for {
    regex <- Arbitrary.arbitrary[Boolean]
    caseSensitive <- Arbitrary.arbitrary[Boolean]
    filePath <- Gen.nonEmptyListOf(Gen.nonEmptyStringOf(Gen.alphaChar)).map(_.mkString(""))
  } yield {
    if (regex && caseSensitive) {
      Identifier.ofFilePath(s"(?i)$filePath:.*".r)
    } else if (regex) {
      Identifier.ofFilePath(s"$filePath:.*".r)
    } else {
      Identifier.ofFilePath(s"$filePath", caseSensitive)
    }
  }

  private val genGav: Gen[Identifier] = for {
    regex <- Arbitrary.arbitrary[Boolean]
    caseSensitive <- Arbitrary.arbitrary[Boolean]
    groupId <- Gen.nonEmptyListOf(Gen.nonEmptyStringOf(Gen.alphaChar)).map(_.mkString("."))
    artifactId <- Gen.nonEmptyListOf(Gen.nonEmptyStringOf(Gen.alphaChar)).map(_.mkString("-"))

    version <- Gen.listOfN(3, Gen.nonEmptyStringOf(Gen.numChar)).map(_.mkString("."))
  } yield {
    if (regex && caseSensitive) {
      Identifier.ofGav(s"(?i)$groupId:$artifactId:.*".r)
    } else if (regex) {
      Identifier.ofGav(s"$groupId:$artifactId:.*".r)
    } else {
      Identifier.ofGav(s"$groupId:$artifactId:$version", caseSensitive)
    }
  }

  private val genPackageUrl: Gen[Identifier] = for {
    regex <- Arbitrary.arbitrary[Boolean]
    caseSensitive <- Arbitrary.arbitrary[Boolean]
    groupId <- Gen.nonEmptyListOf(Gen.nonEmptyStringOf(Gen.alphaChar)).map(_.mkString("."))
    artifactId <- Gen.nonEmptyListOf(Gen.nonEmptyStringOf(Gen.alphaChar)).map(_.mkString("-"))

    version <- Gen.listOfN(3, Gen.nonEmptyStringOf(Gen.numChar)).map(_.mkString("."))
  } yield {
    if (regex && caseSensitive) {
      Identifier.ofPackageUrl(s"(?i)$$pkg:maven/$groupId/$artifactId/.*".r)
    } else if (regex) {
      Identifier.ofPackageUrl(s"$$pkg:maven/$groupId/$artifactId/.*".r)
    } else {
      Identifier.ofPackageUrl(s"pkg:maven/$groupId/$artifactId/$version", caseSensitive)
    }
  }

  private val genIdentifier: Gen[Identifier] =
    Gen.oneOf(
      Gen
        .listOfN(40, Gen.hexChar)
        .map(chars => Identifier.ofSha1(chars.mkString(""))),
      genPackageUrl,
      genFilePath,
      genGav
    )

  private val genCpe: Gen[PropertyType] = for {
    regex <- Arbitrary.arbitrary[Boolean]
    caseSensitive <- Arbitrary.arbitrary[Boolean]
    groupId <- Gen.nonEmptyListOf(Gen.nonEmptyStringOf(Gen.alphaLowerChar)).map(_.mkString("."))
    artifactId <- Gen.nonEmptyListOf(Gen.nonEmptyStringOf(Gen.alphaLowerChar)).map(_.mkString("-"))
  } yield {
    if (regex && caseSensitive) {
      PropertyType.regex(s"(?i)cpe:/a:$groupId:$artifactId:.*".r)
    } else if (regex) {
      PropertyType.regex(s"cpe:/a:$groupId:$artifactId:.*".r)
    } else {
      PropertyType.string(s"cpe:/a:$groupId:$artifactId", caseSensitive)
    }
  }

  private val genCVE: Gen[String] = {
    def lpad(c: Char, n: Int)(s: String): String = (c.toString * (n - s.length)) + s

    for {
      year <- Gen.choose(2009, 2025)
      id <- Gen.choose(1, 99999999)
    } yield s"CVE-$year-${lpad('0', 4)(id.toString)}"
  }

  private lazy val genRule: Gen[SuppressionRule] = for {
    base <- Arbitrary.arbitrary[Boolean]
    id <- Gen.option(genIdentifier)
    cvssBelow <- Gen.listOf(
      Gen.choose(0.0, 10.0).map(d => BigDecimal(d).setScale(2, RoundingMode.HALF_UP).toDouble)
    )
    cwe <- Gen.listOf(Gen.choose(1, 100000).map(_.toString()))
    cve <- Gen.listOf(genCVE)
    vulnerabilityNames <- Gen.listOf(genCVE.map(PropertyType.string(_, caseSensitive = false)))
    cpe <- Gen.listOf(genCpe)
    notes <- Gen.nonEmptyStringOf(Gen.asciiPrintableChar)
    until <- Gen.oneOf(
      Gen.const(LocalDate.EPOCH),
      Gen.choose(-365, 365).map(LocalDate.now().plusDays(_))
    )
    value <-
      if (
        cvssBelow.nonEmpty || cve.nonEmpty || cwe.nonEmpty || cpe.nonEmpty || vulnerabilityNames.nonEmpty
      ) {
        Gen.const(
          id.map { ident =>
            SuppressionRule.ofIdentifier(
              ident,
              base               = base,
              until              = until,
              cpe                = cpe,
              cvssBelow          = cvssBelow,
              cwe                = cwe,
              cve                = cve,
              vulnerabilityNames = vulnerabilityNames,
              notes              = notes
            )
          }.getOrElse(
            SuppressionRule(
              base               = base,
              until              = until,
              cpe                = cpe,
              cvssBelow          = cvssBelow,
              cwe                = cwe,
              cve                = cve,
              vulnerabilityNames = vulnerabilityNames,
              notes              = notes
            )
          )
        )
      } else {
        Gen.fail[SuppressionRule]
      }
  } yield value

}
