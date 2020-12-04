package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataAdmission, TestHelper, TestSetup }

class AdmissionErrorTests extends TestBase {

  var chaincodeConnection: ConnectionAdmissionTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    // TODO: RESET LEDGER
    TestSetup.setupExaminationRegulations(initializeExaminationRegulation())
    TestSetup.setupMatriculations(initializeMatriculation())
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    chaincodeConnection = initializeAdmission()
  }

  override def afterEach(): Unit = {
    chaincodeConnection.close()
    super.afterEach()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    // TODO: RESET LEDGER
    super.afterAll()
  }

  "The ScalaAPI for Admissions" when {
    "invoked with addAdmission incorrectly " should {
      "not allow for adding duplicate Admission with admissionId" in {
        // initial Add
        TestHelper.testAddAdmissionAccess(chaincodeConnection, TestDataAdmission.admission1)

        val result = intercept[TransactionExceptionTrait](chaincodeConnection.addAdmission(TestDataAdmission.admission1))
        result.transactionName should be("addAdmission")
        // TODO compare errors
        // result.payload should be("")
      }
      "allow for adding new Admission without admissionId" in {
        // initial Add
        TestHelper.testAddAdmissionAccess(chaincodeConnection, TestDataAdmission.admission_noAdmissionId)

        val result = intercept[TransactionExceptionTrait](chaincodeConnection.addAdmission(TestDataAdmission.admission_noAdmissionId))
        result.transactionName should be("addAdmission")
        // TODO compare errors
        // result.payload should be("")
      }
    }

    "invoked with getAdmissions incorrectly " should {
      "not be possible" in {
        // TODO find possible errors
        true should be(true)
      }
    }

    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED AFTER THE addAdmission-TESTS.
    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED SEQUENTIALLY IN THIS EXACT ORDER.
    "invoked with dropAdmission incorrectly " should {
      "not allow for dropping existing Admission a second time " in {
        // prepare empty ledger
        chaincodeConnection.dropAdmission("AdmissionStudent_1:C.1")

        // test exception
        val result = intercept[TransactionExceptionTrait](chaincodeConnection.dropAdmission("AdmissionStudent_1:C.1"))
        result.transactionName should be("dropAdmission")
        // TODO compare errors
        // result.payload should be("")

        // check ledger state
        val ledgerAdmissions = chaincodeConnection.getAdmissions()
        val expectedResult = TestHelper.getJsonList(Seq(TestDataAdmission.admission_noAdmissionId_WithId))
        TestHelper.compareJson(expectedResult, ledgerAdmissions)
      }
    }
  }
}