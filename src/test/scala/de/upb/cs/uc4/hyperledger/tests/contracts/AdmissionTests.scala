package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.{ TestDataAdmission, TestDataMatriculation, TestHelper, TestHelperStrings, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

import scala.util.Using

class AdmissionTests extends TestBase {

  var chaincodeConnection: ConnectionAdmissionTrait = _

  val testUser1 = "AdmissionStudent_1"
  val testUser2 = "AdmissionStudent_2"

  override def beforeAll(): Unit = {
    super.beforeAll()
    // TODO: RESET LEDGER
    TestSetup.establishAdminGroup(initializeGroup(), username);
    TestSetup.setupExaminationRegulations(initializeExaminationRegulation())
    setupMatriculations()
  }

  def setupMatriculations(): Unit = {
    Using(initializeMatriculation()) {
      matConnection: ConnectionMatriculationTrait =>
        {
          // prepare users
          prepareUser(testUser1)
          prepareUser(testUser2)

          // prepare data
          val mat1 = TestDataMatriculation.validMatriculationDataCustom(testUser1, "AdmissionER_Open1")
          val mat2 = TestDataMatriculation.validMatriculationDataCustom(testUser2, "AdmissionER_Closed1")

          // approve as Users
          initializeOperation(testUser1).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", mat1)
          initializeOperation(testUser2).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", mat2)
          initializeOperation(username).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", mat1)
          initializeOperation(username).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", mat2)

          // store on chain
          TestHelper.trySetupConnections(
            "setupMatriculations",
            () => { matConnection.addMatriculationData(mat1) },
            () => { matConnection.addMatriculationData(mat2) }
          )
        }
    }
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
    "invoked with addAdmission correctly " should {
      "allow for adding new Admission with admissionId" in {
        TestHelper.testAddAdmissionAccess(chaincodeConnection, TestDataAdmission.admission1)
      }
      "allow for adding new Admission without admissionId" in {
        val testResult = chaincodeConnection.addAdmission(TestDataAdmission.admission_noAdmissionId)
        TestHelper.compareAdmissions(TestDataAdmission.admission_noAdmissionId_WithId, testResult)
      }
      "allow for adding new Admission for closed ER" in {
        TestHelper.testAddAdmissionAccess(chaincodeConnection, TestDataAdmission.admission2)
      }
    }

    "invoked with getAdmissions correctly " should {
      val testData: Seq[(String, String, String, String, Seq[String])] = Seq(
        ("allow for getting all admissions []", "", "", "", Seq(TestDataAdmission.admission1, TestDataAdmission.admission_noAdmissionId_WithId, TestDataAdmission.admission2)),
        ("allow for getting all admissions for user [AdmissionStudent_1]", "AdmissionStudent_1", "", "", Seq(TestDataAdmission.admission1, TestDataAdmission.admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for user [AdmissionStudent_2]", "AdmissionStudent_2", "", "", Seq(TestDataAdmission.admission2)),
        ("allow for getting all admissions for course [C.1]", "", "C.1", "", Seq(TestDataAdmission.admission1)),
        ("allow for getting all admissions for course [C.2]", "", "C.2", "", Seq(TestDataAdmission.admission_noAdmissionId_WithId, TestDataAdmission.admission2)),
        ("allow for getting all admissions for module [AdmissionModule_1]", "", "", "AdmissionModule_1", Seq(TestDataAdmission.admission1, TestDataAdmission.admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for module [AdmissionModule_2]", "", "", "AdmissionModule_2", Seq()),
        ("allow for getting all admissions for module [AdmissionModule_3]", "", "", "AdmissionModule_3", Seq(TestDataAdmission.admission2)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and course [C.1]", "AdmissionStudent_1", "C.1", "", Seq(TestDataAdmission.admission1)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and course [C.2]", "AdmissionStudent_1", "C.2", "", Seq(TestDataAdmission.admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and course [C.3]", "AdmissionStudent_1", "C.3", "", Seq()),
        ("allow for getting all admissions for user [AdmissionStudent_1] and module [AdmissionModule_1]", "AdmissionStudent_1", "", "AdmissionModule_1", Seq(TestDataAdmission.admission1, TestDataAdmission.admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and module [AdmissionModule_2]", "AdmissionStudent_1", "", "AdmissionModule_2", Seq()),
      )
      for ((statement: String, enrollmentId: String, courseId: String, moduleId: String, admissions: Seq[String]) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          val testResult = chaincodeConnection.getAdmissions(enrollmentId, courseId, moduleId)
          val expectedResult = TestHelperStrings.getJsonList(admissions)

          TestHelperStrings.compareJson(expectedResult, testResult)
        }
      }
    }

    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED AFTER THE addAdmission-TESTS.
    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED SEQUENTIALLY IN THIS EXACT ORDER.
    "invoked with dropAdmission correctly " should {
      val testData: Seq[(String, String, Seq[String])] = Seq(
        ("allow for dropping existing Admission 1", "AdmissionStudent_1:C.1", Seq(TestDataAdmission.admission_noAdmissionId_WithId, TestDataAdmission.admission2)),
        ("allow for dropping existing Admission 2", "AdmissionStudent_2:C.2", Seq(TestDataAdmission.admission_noAdmissionId_WithId)),
        ("allow for dropping existing Admission 3", "AdmissionStudent_1:C.2", Seq())
      )
      for ((statement: String, admissionId: String, remainingAdmissions: Seq[String]) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          val testResult = chaincodeConnection.dropAdmission(admissionId)
          testResult should be("")

          // check ledger state
          val ledgerAdmissions = chaincodeConnection.getAdmissions()
          val expectedResult = TestHelperStrings.getJsonList(remainingAdmissions)
          TestHelperStrings.compareJson(expectedResult, ledgerAdmissions)
        }
      }
    }
  }

  "The ScalaAPI for Admissions" when {
    "invoked with addAdmission incorrectly " should {
      "not allow for adding duplicate Admission with admissionId" in {
        // initial Add
        chaincodeConnection.addAdmission(TestDataAdmission.admission1)

        val result = intercept[TransactionExceptionTrait](chaincodeConnection.addAdmission(TestDataAdmission.admission1))
        result.transactionName should be("addAdmission")
        // TODO compare errors
        // result.payload should be("")
      }
      "not allow for adding duplicate Admission without admissionId" in {
        // initial Add
        chaincodeConnection.addAdmission(TestDataAdmission.admission_noAdmissionId)

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
        val expectedResult = TestHelperStrings.getJsonList(Seq(TestDataAdmission.admission_noAdmissionId_WithId))
        TestHelperStrings.compareJson(expectedResult, ledgerAdmissions)
      }
    }
  }
}