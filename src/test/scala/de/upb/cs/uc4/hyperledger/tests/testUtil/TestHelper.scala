package de.upb.cs.uc4.hyperledger.tests.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import org.scalatest.matchers.should.Matchers._

object TestHelper {

  def testAddCertificateAccess(testId: String, certificateConnection: ConnectionCertificateTrait): Unit = {
    val testCert = "whatever"
    certificateConnection.addCertificate(testId, testCert)
    val result: String = certificateConnection.getCertificate(testId)
    result should be(testCert)
  }

  def compareCertificates(expected: String, actual: String): Unit = {
    val cleanExpected = expected
    val cleanActual = actual
    cleanActual should be(cleanExpected)
  }

  def compareJson(expected: String, actual: String): Unit = {
    val cleanExpected = cleanJson(expected)
    val cleanActual = cleanJson(actual)
    cleanActual should be(cleanExpected)
  }

  def cleanJson(input: String): String = {
    input
      .replace("\n", "")
      .replace(" ", "")
  }
}
