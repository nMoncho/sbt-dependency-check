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

package net.nmoncho.sbt.dependencycheck.settings

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.regex.Pattern

import scala.util.Try
import scala.util.matching.Regex
import scala.xml.Elem
import scala.xml.PCData

import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule._
import net.nmoncho.sbt.dependencycheck.tasks.logThrowable
import org.owasp.dependencycheck.xml.suppression.{ PropertyType => OwaspPropertyType }
import org.owasp.dependencycheck.xml.suppression.{ SuppressionRule => OwaspSuppressionRule }
import sbt.Logger

/** Scala counterpart of [[org.owasp.dependencycheck.xml.suppression.SuppressionRule]]
  *
  *  See <a href="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">Dependency Suppression XSD</a>
  *
  * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
  * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
  * @param identifier can be a filePath, GAV, SHA1, or PackageUrl
  * @param cpe A list of CPEs to suppress
  * @param cvssBelow list of cvssBelow scores
  * @param cwe list of CWE entries to suppress
  * @param cve list of CVE entries to suppress
  * @param vulnerabilityNames vulnerability name entries to suppress
  * @param notes notes added in suppression file
  */
case class SuppressionRule private (
    base: Boolean,
    until: LocalDate,
    identifier: Option[Identifier],
    cpe: Seq[PropertyType],
    cvssBelow: Seq[Double],
    cwe: Seq[String],
    cve: Seq[String],
    vulnerabilityNames: Seq[PropertyType],
    notes: String
) {

  def toOwasp: OwaspSuppressionRule = {
    import scala.jdk.CollectionConverters.*

    val rule     = new OwaspSuppressionRule()
    val untilCal = Calendar.getInstance()
    untilCal.set(until.getYear, until.getMonthValue - 1, until.getDayOfMonth, 0, 0, 0)

    rule.setBase(base)
    rule.setUntil(untilCal)
    identifier match {
      case Some(Identifier(id, FilePath)) => rule.setFilePath(id.toOwasp)
      case Some(Identifier(id, Gav)) => rule.setGav(id.toOwasp)
      case Some(Identifier(id, PackageUrl)) => rule.setPackageUrl(id.toOwasp)
      case Some(Identifier(id, Sha1)) => rule.setSha1(id.value)
      case None =>
    }
    rule.setCpe(cpe.map(_.toOwasp).asJava)
    rule.setCvssBelow(cvssBelow.map(x => x: java.lang.Double).asJava)
    rule.setCwe(cwe.asJava)
    rule.setCve(cve.asJava)
    vulnerabilityNames.foreach(vn => rule.addVulnerabilityName(vn.toOwasp))
    rule.setNotes(notes)

    rule
  }

  /** Converts this rule to a `suppress` XML element
    *
    * @return xml element
    */
  def toOwaspXml: Elem = {
    // format: off
    val id = identifier.flatMap {
      case Identifier(PropertyType.Empty, _) => None
      case Identifier(id, FilePath) => Some(<filePath regex={id.regex.toString} caseSensitive={id.caseSensitive.toString}>{id.value}</filePath>)
      case Identifier(id, Gav) => Some(<gav regex={id.regex.toString} caseSensitive={id.caseSensitive.toString}>{id.value}</gav>)
      case Identifier(id, Sha1) => Some(<sha1>{id.value}</sha1>)
      case Identifier(id, PackageUrl) => Some(<packageUrl regex={id.regex.toString} caseSensitive={id.caseSensitive.toString}>{id.value}</packageUrl>)
    }

    // Just like any XML, the order of the elements is important. This is taken from the XSD
    val body =
        Option(notes).filter(_.nonEmpty).map(_ => <notes>{PCData(notes)}</notes>).toSeq ++
        id.toSeq ++
        cpe.map(c => <cpe regex={c.regex.toString} caseSensitive={c.caseSensitive.toString}>{c.value}</cpe>) ++
        cvssBelow.map(cvss => <cvssBelow>{cvss}</cvssBelow>) ++
        cwe.map(c => <cwe>{c}</cwe>) ++
        cve.map(c => <cve>{c}</cve>) ++
        vulnerabilityNames.map(v => <vulnerabilityName regex={v.regex.toString} caseSensitive={v.caseSensitive.toString}>{v.value}</vulnerabilityName>)
    // format: on

    if (until.compareTo(LocalDate.EPOCH) <= 0) {
      <suppress base={base.toString}>
        {body}
      </suppress>
    } else {
      <suppress base={base.toString} until={until.format(DateFormatter)}>
        {body}
      </suppress>
    }
  }
}

object SuppressionRule {

  private val DateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  /** A [[SuppressionRule]] can be used against one optional identifier
    *
    * This identifier can be a regex, matching multiple artifacts.
    */
  sealed trait IdentifierType
  case object FilePath extends IdentifierType
  case object Gav extends IdentifierType
  case object Sha1 extends IdentifierType
  case object PackageUrl extends IdentifierType

  case class Identifier private (id: PropertyType, `type`: IdentifierType)

  object Identifier {

    // format: off
    private[settings] def apply(id: PropertyType, `type`: IdentifierType): Identifier = new Identifier(id, `type`)

    private def ofString(value: String, caseSensitive: Boolean, `type`: IdentifierType): Identifier =
      new Identifier(PropertyType.string(value, caseSensitive), `type`)

    private def ofRegex(value: Regex, `type`: IdentifierType): Identifier =
      new Identifier(PropertyType.regex(value), `type`)

    def ofFilePath(value: String, caseSensitive: Boolean): Identifier = ofString(value, caseSensitive, FilePath)

    def ofFilePath(value: Regex): Identifier = ofRegex(value, FilePath)

    def ofGav(value: String, caseSensitive: Boolean): Identifier = ofString(value, caseSensitive, Gav)

    def ofGav(value: Regex): Identifier = ofRegex(value, Gav)

    def ofPackageUrl(value: String, caseSensitive: Boolean): Identifier = ofString(value, caseSensitive, PackageUrl)

    def ofPackageUrl(value: Regex): Identifier = ofRegex(value, PackageUrl)

    def ofSha1(value: String): Identifier = ofString(value, caseSensitive = false, Sha1)
    // format: on
  }

  /** Scala counterpart for XML's `regexStringType`
    *
    * @param value string value
    * @param regex whether is a regex or not
    * @param caseSensitive whether is case sensitive or not
    */
  case class PropertyType(value: String, regex: Boolean, caseSensitive: Boolean) {

    def toOwasp: OwaspPropertyType = {
      val prop = new OwaspPropertyType()

      prop.setValue(value)
      prop.setRegex(regex)
      prop.setCaseSensitive(caseSensitive)

      prop
    }

  }

  object PropertyType {
    val Empty: PropertyType = PropertyType("", regex = false, caseSensitive = false)

    def regex(value: Regex): PropertyType =
      PropertyType(
        value         = value.regex,
        regex         = true,
        caseSensitive = (value.pattern.flags() & Pattern.CASE_INSENSITIVE) == 0
      )

    def string(value: String, caseSensitive: Boolean): PropertyType =
      PropertyType(
        value         = value,
        regex         = false,
        caseSensitive = caseSensitive
      )

    def fromOwasp(prop: org.owasp.dependencycheck.xml.suppression.PropertyType): PropertyType =
      new PropertyType(prop.getValue, prop.isRegex, prop.isCaseSensitive)
  }

  /** Converts [[org.owasp.dependencycheck.xml.suppression.SuppressionRule]] into a [[SuppressionRule]]
    */
  def fromOwasp(rule: OwaspSuppressionRule)(implicit log: Logger): SuppressionRule = {
    import scala.jdk.CollectionConverters.*

    def getPackageUrl(): Option[PropertyType] = Try {
      classOf[OwaspSuppressionRule].getDeclaredFields
        .find(_.getName == "packageUrl")
        .map { field =>
          field.setAccessible(true)
          PropertyType.fromOwasp(field.get(rule).asInstanceOf[OwaspPropertyType])
        }
    }.recover { case t: Throwable =>
      log.warn("Failed to get 'packageUrl' using reflection")
      logThrowable(t)
      None
    }.toOption
      .flatten

    def getVulnerabilityNames(): Seq[PropertyType] = Try {
      classOf[OwaspSuppressionRule].getDeclaredFields
        .find(_.getName == "vulnerabilityNames")
        .map { field =>
          field.setAccessible(true)
          field
            .get(rule)
            .asInstanceOf[java.util.List[OwaspPropertyType]]
            .asScala
            .map(PropertyType.fromOwasp)
        }
    }.recover { case t: Throwable =>
      log.warn("Failed to get 'vulnerabilityNames' using reflection")
      logThrowable(t)
      t.printStackTrace()
      None
    }.toOption
      .flatten
      .getOrElse(Seq.empty[PropertyType])

    val identifier = if (rule.hasGav) {
      Some(Identifier(PropertyType.fromOwasp(rule.getGav), Gav))
    } else if (rule.getFilePath != null) {
      Some(Identifier(PropertyType.fromOwasp(rule.getFilePath), FilePath))
    } else if (rule.getSha1 != null && rule.getSha1.nonEmpty) {
      Some(Identifier.ofSha1(rule.getSha1))
    } else if (rule.hasPackageUrl) {
      getPackageUrl().map(Identifier(_, PackageUrl))
    } else {
      None
    }

    val until = Option(rule.getUntil)
      .map { c =>
        LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE))
      }
      .getOrElse(LocalDate.EPOCH)

    new SuppressionRule(
      base               = rule.isBase,
      until              = until,
      identifier         = identifier,
      cpe                = rule.getCpe.asScala.map(PropertyType.fromOwasp).toList,
      cvssBelow          = rule.getCvssBelow.asScala.map(_.toDouble).toList,
      cwe                = rule.getCwe.asScala.toList,
      cve                = rule.getCve.asScala.toList,
      vulnerabilityNames = getVulnerabilityNames().toList,
      notes              = Option(rule.getNotes).getOrElse("")
    )
  }

  // format: off

  /** Creates a [[SuppressionRule]] that doesn't target a particular filePath, gav, sha1, nor packageUrl
   *
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def apply(base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    new SuppressionRule(base, until, None, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)

  /** Creates a [[SuppressionRule]] for a file path
   *
   * @param value file path as a string (if you want to use a regex see the overloaded version)
   * @param caseSensitive whether the file path should be treated case sensitive or not.
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def ofFilePath(value: String, caseSensitive: Boolean = false, base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    ofIdentifier(Identifier.ofFilePath(value, caseSensitive), base, until, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)

  /** Creates a [[SuppressionRule]] for a file path using a regex
   *
   * @param value file path as a regex
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def ofFilePathRegex(value: Regex, base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    ofIdentifier(Identifier.ofFilePath(value), base, until, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)

  /** Creates a [[SuppressionRule]] for a gav
   *
   * @param value gav as a string (if you want to use a regex see the overloaded version)
   * @param caseSensitive whether the file path should be treated case sensitive or not.
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def ofGav(value: String, caseSensitive: Boolean = false, base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    ofIdentifier(Identifier.ofGav(value, caseSensitive), base, until, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)

  /** Creates a [[SuppressionRule]] for a GAV using a regex
   *
   * @param value GAV as a regex
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def ofGavRegex(value: Regex, base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    ofIdentifier(Identifier.ofGav(value), base, until, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)

  /** Creates a [[SuppressionRule]] for a Package URL
   *
   * @param value Package URL as a string (if you want to use a regex see the overloaded version)
   * @param caseSensitive whether the file path should be treated case sensitive or not.
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def ofPackageUrl(value: String, caseSensitive: Boolean = false, base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    ofIdentifier(Identifier.ofPackageUrl(value, caseSensitive), base, until, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)

  /** Creates a [[SuppressionRule]] for a Package URL using a regex
   *
   * @param value Package URL as a regex
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def ofPackageUrlRegex(value: Regex, base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    ofIdentifier(Identifier.ofPackageUrl(value), base, until, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)

  /** Creates a [[SuppressionRule]] for a file with a given SHA1
   *
   * @param value sha1 value
   * @param base A flag indicating whether or not the suppression rule is a core/ base rule that should not be included in the resulting report in the "suppressed" section.
   * @param until A date until which the suppression is to be retained. This can be used to make a temporary suppression that auto-expires to suppress a CVE while waiting for the vulnerability fix of the dependency to be released.
   * @param cpe A list of CPEs to suppress
   * @param cvssBelow list of cvssBelow scores
   * @param cwe list of CWE entries to suppress
   * @param cve list of CVE entries to suppress
   * @param vulnerabilityNames vulnerability name entries to suppress
   */
  def ofSha1(value: String, base: Boolean = false, cpe: Seq[PropertyType] = Seq.empty, cvssBelow: Seq[Double] = Seq.empty, cwe: Seq[String] = Seq.empty, cve: Seq[String] = Seq.empty, vulnerabilityNames: Seq[PropertyType] = Seq.empty, notes: String = "", until: LocalDate = LocalDate.EPOCH): SuppressionRule =
    ofIdentifier(Identifier.ofSha1(value), base, until, cpe, cvssBelow, cwe, cve, vulnerabilityNames, notes)
  // format: on

  /** Helper method to create a [[SuppressionRule]] for a given identifier
    */
  private[settings] def ofIdentifier(
      identifier: Identifier,
      base: Boolean,
      until: LocalDate,
      cpe: Seq[PropertyType],
      cvssBelow: Seq[Double],
      cwe: Seq[String],
      cve: Seq[String],
      vulnerabilityNames: Seq[PropertyType],
      notes: String
  ): SuppressionRule =
    new SuppressionRule(
      base               = base,
      until              = until,
      identifier         = Some(identifier),
      cpe                = cpe,
      cvssBelow          = cvssBelow,
      cwe                = cwe,
      cve                = cve,
      vulnerabilityNames = vulnerabilityNames,
      notes              = notes
    )

  /** Generates a Suppression File XML content
    *
    * @param rules rules to be in the xml file.
    * @return formatted XML as a String
    */
  def toSuppressionsXML(rules: Seq[SuppressionRule]): String = {
    val xml =
      <suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
      {rules.map(_.toOwaspXml)}
      </suppressions>

    new scala.xml.PrettyPrinter(120, 4).format(xml)
  }
}
