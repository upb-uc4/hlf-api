package de.upb.cs.uc4.hyperledger.tests.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCertificateTrait, ConnectionExaminationRegulationTrait}
import de.upb.cs.uc4.hyperledger.utilities.helper.ReflectionHelper
import de.upb.cs.uc4.hyperledger.utilities.{EnrollmentManager, RegistrationManager}
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.scalatest.matchers.should.Matchers._

import java.security.cert.X509Certificate
import java.util.Base64
import scala.util.matching.Regex

object TestHelper {

  /// EXAMINATION REGULATIONS
  def testAddExaminationRegulationAccess(connection: ConnectionExaminationRegulationTrait, name: String, modules: Array[String], state: Boolean): Unit = {
    val testObject = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state);
    val testResult = connection.addExaminationRegulation(testObject)

    compareExaminationRegulations(testObject, testResult)
  }
  def compareExaminationRegulations(testObject: String, testResult: String): Unit = {
    compareJson(testObject, testResult)
  }

  /// CERTIFICATES
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

  /// GENERAL
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
  def getJsonList(modules: Array[String]): String = {
    "[" + modules.tail.fold(modules.head)((A, B) => A + "," + B) + "]"
  }

  def toPemString(certificate: X509Certificate): String = {
    import sun.security.provider.X509Factory
    s"${X509Factory.BEGIN_CERT}\n${Base64.getEncoder.encodeToString(certificate.getEncoded).replaceAll(".{64}", "$0\n")}\n${X509Factory.END_CERT}\n"
  }

  def getCryptoPrimitives(): CryptoPrimitives = {
    val crypto: CryptoPrimitives = new CryptoPrimitives()
    val securityLevel: Integer = 256
    ReflectionHelper.safeCallPrivateMethod(crypto)("setSecurityLevel")(securityLevel)
    crypto
  }
}
