package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.TestHelper
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class CertificateErrorTests extends TestBase {

  var chaincodeConnection: ConnectionCertificateTrait = _
  val TestEnrollmentID = "001"
  val TestCertificate = "001"

  override def beforeAll(): Unit = {
    super.beforeAll()
    try {
      chaincodeConnection = initializeCertificate()
      chaincodeConnection.addCertificate(TestEnrollmentID, TestCertificate)
    }
    catch {
      case e: Exception => Logger.err("[CertificateErrorTest] :: ", e)
    }
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Certificate" when {
    "invoking getCertificate" should {
      val testData: Seq[(String, String)] = Seq(
        ("throw TransactionException for not existing enrollmentId [000]", "000"),
        ("throw TransactionException for empty enrollmentId-String []", ""),
        ("throw TransactionException if enrollmentId-String equals null", null)
      )
      for ((statement: String, data: String) <- testData) {
        s"$statement" in {
          TestHelper.testTransactionException("getCertificate", () => chaincodeConnection.getCertificate(data))
        }
      }
    }

    "invoking addCertificate" should {
      val testData: Seq[(String, String, String)] = Seq(
        ("throw TransactionException for existing enrollmentId [001] ", this.TestEnrollmentID, TestCertificate),
        ("throw TransactionException for empty enrollmentId-String ", "", TestCertificate),
        ("throw TransactionException if enrollmentId-String equals null", null, TestCertificate),
        ("throw TransactionException for empty certificate-String [002]", "002", ""),
        ("throw TransactionException if certificate-String equals null [003]", "003", null),
        ("throw TransactionException for empty enrollmentID-String and empty certificate-String ", "", ""),
        ("throw TransactionException if enrollmentID-String and certificate-String equal null", null, null)
      )
      for ((statement: String, enrollmentId: String, certificate: String) <- testData) {
        s"$statement" in {
          TestHelper.testTransactionException("addCertificate", () => chaincodeConnection.addCertificate(enrollmentId, certificate))
        }
      }
    }

    "invoking updateCertificate" should {
      val testData: Seq[(String, String, String)] = Seq(
        ("throw TransactionException for not existing enrollmentId [004] ", "004", TestCertificate),
        ("throw TransactionException for empty enrollmentId-String ", "", TestCertificate),
        ("throw TransactionException if enrollmentId-String equals null", null, TestCertificate),
        ("throw TransactionException for empty certificate-String [001] ", TestEnrollmentID, ""),
        ("throw TransactionException if certificate-String equals null", TestEnrollmentID, null),
        ("throw TransactionException for empty enrollmentID-String and empty certificate-String ", "", ""),
        ("throw TransactionException if enrollmentID-String and certificate-String equal null", null, null)
      )
      for ((statement: String, enrollmentId: String, certificate: String) <- testData) {
        s"$statement" in {
          TestHelper.testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate(enrollmentId, certificate))
        }
      }
    }
  }
}
