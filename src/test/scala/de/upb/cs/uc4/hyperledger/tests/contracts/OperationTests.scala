package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionOperationsTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class OperationTests extends TestBase {

  var chaincodeConnection: ConnectionOperationsTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeOperation()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Operations used correctly " when {
    "invoked with approveTransaction" should {
      "allow for adding new Approval " in {
        chaincodeConnection.approveTransaction(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert")
      }
      "allow for adding existing Approval a second time" in {
        chaincodeConnection.approveTransaction(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert")
      }
    }
  }

  "The ScalaAPI for Operations used incorrectly " when {
    "invoked with approveTransaction" should {
      "deny adding new Approval with empty transactionId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.approveTransaction(username, "UC4.Certificate", "", "000001", "totally valid cert")
        }
        exceptionResult.transactionName should be("approveTransaction")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
      "deny adding new Approval with empty contractId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          val result = chaincodeConnection.approveTransaction(username, "", "addCertificate", "000001", "totally valid cert")
          Logger.debug("APPROVAL RESULT :: " + result)
        }
        exceptionResult.transactionName should be("approveTransaction")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"contractName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
      "deny adding new Approval with wrong number of parameters" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.approveTransaction(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert", "weird third parameter")
        }

        Logger.debug(exceptionResult.toString)
      }
    }
  }
}
