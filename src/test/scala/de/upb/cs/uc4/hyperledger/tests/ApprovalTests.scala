package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionApprovalsTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class ApprovalTests extends TestBase {

  var chaincodeConnection: ConnectionApprovalsTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeApproval()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Approvals" when {
    "invoked for existing contract-transactions " should {
      "allow for adding new AprovalData " in {
        chaincodeConnection.approveTransaction("UC4.Certificate", "addCertificate", "000001", "totally valid cert")
      }
      "allow for adding existing new AprovalData a second time" in {
        chaincodeConnection.approveTransaction("UC4.Certificate", "addCertificate", "000001", "totally valid cert")
      }
      "allow for adding new AprovalData with wrong number of parameters" in {
        chaincodeConnection.approveTransaction("UC4.Certificate", "addCertificate", "000001", "totally valid cert", "weird third parameter")
      }
    }
    "invoked for empty transaction " should {
      "prohibit manipulation" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.approveTransaction("UC4.Certificate", "", "000001", "totally valid cert")
        }
        exceptionResult.transactionName should be("approveTransaction")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
    }
    "invoked for empty Contract " should {
      "prohibit manipulation" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          val result = chaincodeConnection.approveTransaction("", "addCertificate", "000001", "totally valid cert")
          Logger.debug("APPROVAL RESULT :: " + result)
        }
        exceptionResult.transactionName should be("approveTransaction")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"contractName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
    }
  }
}