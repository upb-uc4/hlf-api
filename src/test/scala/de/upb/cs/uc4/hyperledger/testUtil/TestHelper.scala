package de.upb.cs.uc4.hyperledger.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionCertificateTrait, ConnectionExaminationRegulationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.protos.peer.TransactionPackage.Transaction
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._

object TestHelper {

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
    payload should include("approveTransaction")
    // payload contains approval parameters (target TransactionInfo)
    contents.foreach(item => clearProtobufInfo(payload) should include(clearProtobufInfo(item)))
  }
  private def clearProtobufInfo(item: String): String = {
    TestHelperStrings.removeNewLinesAndSpaces(item)
      .replace("\\u003d", "=")
      .replace("\\", "")
  }

  /// Admissions
  def testAddAdmissionAccess(connection: ConnectionAdmissionTrait, student: String, course: String, module: String, timestamp: String): Assertion =
    testAddAdmissionAccess(connection, TestDataAdmission.validAdmission(student, course, module, timestamp))
  def testAddAdmissionAccess(connection: ConnectionAdmissionTrait, admission: String): Assertion = {
    val testResult = connection.addAdmission(admission)

    compareAdmissions(admission, testResult)
  }
  def compareAdmissions(testObject: String, testResult: String): Assertion = {
    TestHelperStrings.compareJson(testObject, testResult)
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

  def trySetupConnections(actionName: String, fs: (() => Any)*): Unit = {
    fs.foreach(f => {
      try {
        f.apply()
      }
      catch {
        case e: Throwable => Logger.err(s"Error during $actionName: ", e)
      }
    })
  }
}
