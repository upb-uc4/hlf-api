package de.upb.cs.uc4.hyperledger.tests.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionCertificateTrait, ConnectionExaminationRegulationTrait, ConnectionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._

object TestHelper {

  /// Admissions
  def testAddAdmissionAccess(connection: ConnectionAdmissionTrait, student: String, course: String, module: String, timestamp: String): Assertion =
    testAddAdmissionAccess(connection, TestDataAdmission.validAdmission(student, course, module, timestamp))
  def testAddAdmissionAccess(connection: ConnectionAdmissionTrait, admission: String): Assertion = {
    val testResult = connection.addAdmission(admission)

    compareAdmissions(admission, testResult)
  }
  def compareAdmissions(testObject: String, testResult: String): Assertion = {
    compareJson(testObject, testResult)
  }

  /// EXAMINATION REGULATIONS
  def testAddExaminationRegulationAccess(connection: ConnectionExaminationRegulationTrait, name: String, modules: Seq[String], state: Boolean): Assertion = {
    val testObject = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state)
    val testResult = connection.addExaminationRegulation(testObject)

    compareExaminationRegulations(testObject, testResult)
  }
  def compareExaminationRegulations(testObject: String, testResult: String): Assertion = {
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
  def compareJson(expected: String, actual: String): Assertion = {
    val cleanExpected = cleanJson(expected)
    val cleanActual = cleanJson(actual)
    cleanActual should be(cleanExpected)
  }
  def cleanJson(input: String): String = {
    input
      .replace("\n", "")
      .replace(" ", "")
  }
  def getJsonList(items: Seq[String]): String = {
    "[" + TestHelper.nullableSeqToString(items) + "]"
  }
  def nullableSeqToString(input: Seq[String]): String = {
    if (input == null) ""
    else input.mkString(", ")
  }

  // Exception
  def testTransactionException(transactionName: String, f: () => Any): Assertion = {
    val result = intercept[TransactionExceptionTrait](f.apply())
    result.transactionName should be(transactionName)
  }

  def trySetupConnections(actionName: String, f: ()=>Any): Unit = {
    try {
      f.apply()
    }
    catch {
      case e: Throwable => Logger.err(s"Error during $actionName: ", e)
    }
  }
}
