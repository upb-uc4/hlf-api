package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
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

  private def testTransactionException(transactionName: String, f: () => Any) = {
    val result = intercept[TransactionExceptionTrait](f.apply())
    result.transactionName should be(transactionName)
  }

  "The ScalaAPI for Certificate" when {
    "invoking getCertificate" should {
      "throw TransactionException for not existing enrollmentId [000]" in {
        testTransactionException("getCertificate", () => chaincodeConnection.getCertificate("000"))
      }
      "throw TransactionException for empty enrollmentId-String []" in {
        testTransactionException("getCertificate", () => chaincodeConnection.getCertificate(""))
      }
      "throw TransactionException if enrollmentId-String equals null" in {
        testTransactionException("getCertificate", () => chaincodeConnection.getCertificate(null))
      }
    }

    "invoking addCertificate" should {
      "throw TransactionException for existing enrollmentId [001] " in {
        testTransactionException("addCertificate", () => chaincodeConnection.addCertificate(this.TestEnrollmentID, TestCertificate))
      }
      "throw TransactionException for empty enrollmentId-String " in {
        testTransactionException("addCertificate", () => chaincodeConnection.addCertificate("", TestCertificate))
      }
      "throw TransactionException if enrollmentId-String equals null" in {
        testTransactionException("addCertificate", () => chaincodeConnection.addCertificate(null, TestCertificate))
      }
      "throw TransactionException for empty certificate-String [002]" in {
        testTransactionException("addCertificate", () => chaincodeConnection.addCertificate("002", ""))
      }
      "throw TransactionException if certificate-String equals null [003]" in {
        testTransactionException("addCertificate", () => chaincodeConnection.addCertificate("003", null))
      }
      "throw TransactionException for empty enrollmentID-String and empty certificate-String " in {
        testTransactionException("addCertificate", () => chaincodeConnection.addCertificate("", ""))
      }
      "throw TransactionException if enrollmentID-String and certificate-String equal null" in {
        testTransactionException("addCertificate", () => chaincodeConnection.addCertificate(null, null))
      }
    }

    "invoking updateCertificate" should {
      "throw TransactionException for not existing enrollmentId [004] " in {
        testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate("004", TestCertificate))
      }
      "throw TransactionException for empty enrollmentId-String " in {
        testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate("", TestCertificate))
      }
      "throw TransactionException if enrollmentId-String equals null" in {
        testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate(null, TestCertificate))
      }
      "throw TransactionException for empty certificate-String [001] " in {
        testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate(TestEnrollmentID, ""))
      }
      "throw TransactionException if certificate-String equals null" in {
        testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate(TestEnrollmentID, null))
      }
      "throw TransactionException for empty enrollmentID-String and empty certificate-String " in {
        testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate("", ""))
      }
      "throw TransactionException if enrollmentID-String and certificate-String equal null" in {
        testTransactionException("updateCertificate", () => chaincodeConnection.updateCertificate(null, null))
      }
    }
  }
}
