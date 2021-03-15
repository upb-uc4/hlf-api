package de.upb.cs.uc4.hyperledger.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionCertificateTrait, ConnectionExaminationRegulationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testData.{ TestDataAdmission, TestDataExaminationRegulation }
import de.upb.cs.uc4.hyperledger.testUtil.TestHelperStrings.removeNewLinesAndSpaces
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.protos.peer.TransactionPackage.Transaction
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._

object TestHelper {

  /// TEST TRANSACTION-PROTOBUFF
  def testTransactionBytesContainsInfo(transactionBytes: Array[Byte], contents: Seq[String]): Unit = {
    val transaction = Transaction.parseFrom(transactionBytes).toString
    TestHelper.testProtobufContainsInfo(transaction, contents)
  }
  def testProposalPayloadBytesContainsInfo(proposalBytes: Array[Byte], contents: Seq[String]): Unit = {
    val approvalProposalPayload = Proposal.parseFrom(proposalBytes).getPayload.toStringUtf8
    TestHelper.testProtobufContainsInfo(approvalProposalPayload, contents)
  }
  private def testProtobufContainsInfo(payload: String, contents: Seq[String]): Unit = {
    // payload contains Approval TransactionInfo
    payload should include("UC4.OperationData")
    payload should include("approveOperation")
    // payload contains approval parameters (target TransactionInfo)
    contents.foreach(item => clearProtobufInfo(payload) should include(clearProtobufInfo(item)))
  }
  private def clearProtobufInfo(item: String): String = {
    TestHelperStrings.removeNewLinesAndSpaces(item)
      .replace("\\u003d", "=")
      .replace("\\", "")
  }

  /// ExamResults
  def compareExamResults(testObject: String, testResult: String): Assertion = {
    TestHelperStrings.compareJson(testResult, testObject)
  }

  /// Admissions
  def testAddAdmissionAccess(connection: ConnectionAdmissionTrait, student: String, course: String, module: String, timestamp: String): Assertion =
    testAddAdmissionAccess(connection, TestDataAdmission.validCourseAdmission(student, course, module, timestamp))
  def testAddAdmissionAccess(connection: ConnectionAdmissionTrait, admission: String): Assertion = {
    val testResult = connection.addAdmission(admission)

    compareAdmission(admission, testResult)
  }
  def compareAdmission(testObject: String, testResult: String): Assertion = {
    val timelessTestObject = stripAdmissionOfTimestamp(TestHelperStrings.removeNewLinesAndSpaces(testObject))
    val timelessTestResult = stripAdmissionOfTimestamp(TestHelperStrings.removeNewLinesAndSpaces(testResult))
    TestHelperStrings.compareJson(timelessTestObject, timelessTestResult)
  }
  def stripAdmissionOfTimestamp(str: String): String = {
    str.replaceAll("\"timestamp\":.*?,", "")
  }

  /// EXAMINATION REGULATIONS
  def testAddExaminationRegulationAccess(connection: ConnectionExaminationRegulationTrait, name: String, modules: Seq[String], state: Boolean): Assertion = {
    val testObject = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state)
    val testResult = connection.addExaminationRegulation(testObject)

    compareExaminationRegulations(testObject, testResult)
  }
  def compareExaminationRegulations(testObject: String, testResult: String): Assertion = {
    TestHelperStrings.compareJson(testObject, testResult)
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

  // Exception
  def testTransactionException(transactionName: String, f: () => Any): Assertion = {
    val result = intercept[TransactionExceptionTrait](f.apply())
    result.transactionName should be(transactionName)
  }
  def testTransactionResult(result: TransactionExceptionTrait, expectedTransactionName: String, expectedError: String): Assertion = {
    result.transactionName should be(expectedTransactionName)
    TestHelperStrings.compareJson(expectedError, result.payload)
  }
}
