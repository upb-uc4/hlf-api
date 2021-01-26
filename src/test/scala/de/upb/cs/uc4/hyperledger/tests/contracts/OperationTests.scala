package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionOperationTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.TestHelperStrings
import de.upb.cs.uc4.hyperledger.utilities.helper.{Logger, StringHelper}

class OperationTests extends TestBase {

  var chaincodeConnection: ConnectionOperationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeOperation()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Operations used correctly " when {
    "invoked with proposeTransaction" should {
      "allow for adding new Approval " in {
        chaincodeConnection.initiateOperation(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert")
      }
      "allow for adding existing Approval a second time" in {
        chaincodeConnection.initiateOperation(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert")
      }
    }
    "invoked with rejectOperation" should {
      "allow for rejecting an Operation " in {
        val result = chaincodeConnection.initiateOperation(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert")
        val operationId = StringHelper.getOperationIdFromOperation(result)
        chaincodeConnection.rejectOperation(operationId, "I do not agree")
      }
      "allow for rejecting an Operation check rejection message" in {
        val result = chaincodeConnection.initiateOperation(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert")
        val operationId = StringHelper.getOperationIdFromOperation(result)
        val rejectionResult = chaincodeConnection.rejectOperation(operationId, "I do not agree")
        val rejectionMessage = StringHelper.getRejectionMessageFromOperation(rejectionResult)
        rejectionMessage shouldBe("I do not agree")
      }
      "allow for rejecting an Operation check operationId" in {
        val result = chaincodeConnection.initiateOperation(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert")
        val operationId = StringHelper.getOperationIdFromOperation(result)
        operationId shouldBe(username)
      }
    }
    "invoked with getOperations" should {
      "allow for querying Operations with filter operationIds" in {
        val result = chaincodeConnection.getOperations(List("0rhY7SWcWIb-yjRYLMBzc3r2rZ-ar-n95Tbls6P3ClA="), "", "", "", "", List(""))
        val operationIds = StringHelper.getOperationIdFromOperation(result)
        operationIds should contain(result)
      }
        "allow for querying Operations with filter existingEnrollmentId" in {
        val result = chaincodeConnection.getOperations(List(""), "000001", "", "", "", List(""))
        val existingApprovals = StringHelper.getExistingApprovalsFromOperation(result)
        existingApprovals should contain(result)
        val (users, groups) = StringHelper.getUsersAndGroupsFromApprovalList(result)
        (users, groups) should be(existingApprovals)
      }
      "allow for querying Operations with filter missingEnrollmentId" in {
        val result = chaincodeConnection.getOperations(List(""), "000001", "000001", "", "", List(""))
        val missingApprovals = StringHelper.getMissingApprovalsFromOperation(result)
        missingApprovals should contain(result)
        val (users, groups) = StringHelper.getUsersAndGroupsFromApprovalList(result)
        (users, groups) should be(missingApprovals)
      }
      "allow for querying Operations with filter existingEnrollmentId and missingEnrollmentId" in {
        val result = chaincodeConnection.getOperations(List(""), "000001", "000001", "", "", List(""))
        val existingApprovals = StringHelper.getExistingApprovalsFromOperation(result)
        val missingApprovals = StringHelper.getMissingApprovalsFromOperation(result)
        existingApprovals should contain(result)
        missingApprovals should contain(result)
      }
      "allow for querying Operations with filter initiatorEnrollmentId" in {
        val result = chaincodeConnection.getOperations(List(""), "000001", "", "", "", List(""))
        val initiatorEnrollmentId = StringHelper.getInitiatorEnrollmentIdFromOperation(result)
        initiatorEnrollmentId should be(result)
      }
      "allow for querying Operations with filter states" in {
        val result = chaincodeConnection.getOperations(List(""), "000001", "", "", "", List(""))
        val states = StringHelper.getStateFromOperation(result)
        states should contain(result)
      }
    }
  }

  "The ScalaAPI for Operations used incorrectly " when {
    "invoked with proposeTransaction" should {
      "deny adding new Approval with empty transactionId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.initiateOperation(username, "UC4.Certificate", "", "000001", "totally valid cert")
        }
        exceptionResult.transactionName should be("initiateOperation")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
      "deny adding new Approval with empty contractId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          val result = chaincodeConnection.initiateOperation(username, "", "addCertificate", "000001", "totally valid cert")
          Logger.debug("APPROVAL RESULT :: " + result)
        }
        exceptionResult.transactionName should be("initiateOperation")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"contractName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
      "deny adding new Approval with too few parameters" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.initiateOperation(username, "UC4.Certificate", "addCertificate", "000001")
        }

        Logger.debug(exceptionResult.toString)
        // TODO: check payload
      }
      "deny adding new Approval with too many parameters" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.initiateOperation(username, "UC4.Certificate", "addCertificate", "000001", "totally valid cert", "weird third parameter")
        }

        Logger.debug(exceptionResult.toString)
        // TODO: check payload
      }
    }
    "invoked with rejectOperation" should {
      "deny rejecting an Operation with empty operationId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.rejectOperation("", "do not agree")
        }
        exceptionResult.transactionName should be("rejectOperation")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
      "deny rejecting an Operation with empty rejectMessage" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          chaincodeConnection.rejectOperation("operation1", "")
        }
        exceptionResult.transactionName should be("rejectTransaction")
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
    }
    "invoked with getOperations" should {
      "deny querying an Operation with an invalid filter existingEnrollmentId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          //TODO: what is an invalid filter
          chaincodeConnection.getOperations(List("a"), "", "", "", "", List(""))
        }
        exceptionResult.transactionName should be("getOperations")
        // TODO
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
      "deny querying an Operation with an invalid filter missingEnrollmentId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          //TODO: what is an invalid filter
          chaincodeConnection.getOperations(List(""), "a", "", "", "", List(""))
        }
        exceptionResult.transactionName should be("getOperations")
        // TODO
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
      "deny querying an Operation with an invalid filter initiatorEnrollmentId" in {
        val exceptionResult: TransactionExceptionTrait = intercept[TransactionExceptionTrait] {
          //TODO: what is an invalid filter
          chaincodeConnection.getOperations(List(""), "", "a", "", "", List(""))
        }
        exceptionResult.transactionName should be("getOperations")
        // TODO
        val expectedPayload = "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"transactionName\",\"reason\":\"The given parameter must not be empty\"}]}"
        exceptionResult.payload should be(expectedPayload)
      }
    }
  }
}
