package de.upb.cs.uc4.hyperledger.tests.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionCertificateTrait, ConnectionExaminationRegulationTrait, ConnectionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, ReflectionHelper }
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._

import java.security.cert.X509Certificate
import java.util.Base64
import scala.util.matching.Regex

object TestHelperStrings {
  // JSON
  def getJsonList(items: Seq[String]): String = {
    "[" + nullableSeqToString(items) + "]"
  }
  def compareJson(expected: String, actual: String): Assertion = {
    val cleanExpected = cleanJson(expected)
    val cleanActual = cleanJson(actual)
    cleanActual should be(cleanExpected)
  }
  def cleanJson(input: String): String = {
    removeSpaces(removeNewLines(input))
  }
  def nullableSeqToString(input: Seq[String]): String = {
    if (input == null) ""
    else input.mkString(", ")
  }

  // ADJUSTMENT
  def normalizeLineEnds(item: String): String = {
    item
      .replace("\r\n", "\n")
      .replace("\r", "\n")
  }
  def removeSpaces(item: String): String = {
    item
      .replace(" ", "")
  }
  def removeNewLines(item: String): String = {
    normalizeLineEnds(item)
      .replace("\n", "")
  }
}
