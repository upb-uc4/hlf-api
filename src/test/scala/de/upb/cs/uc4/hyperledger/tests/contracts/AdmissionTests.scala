package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.{ TestDataAdmission, TestDataMatriculation, TestHelper, TestHelperStrings, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

import scala.util.Using

class AdmissionTests extends TestBase {

  var chaincodeConnection: ConnectionAdmissionTrait = _

  val testNamePrefix = "Admission"

  val examReg1: String = testNamePrefix + "_ER_Open1"
  val examReg2: String = testNamePrefix + "_ER_Closed1"

  val testUser1 = "AdmissionStudent_1"
  val testUser2 = "AdmissionStudent_2"
  val testUser3 = "AdmissionStudent_3"

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeAdmission()
    // TODO: RESET LEDGER
    TestSetup.setupExaminationRegulations(initializeExaminationRegulation(), testNamePrefix)
    setupMatriculations()
  }

  def setupMatriculations(): Unit = {
    Using(initializeMatriculation()) {
      matConnection: ConnectionMatriculationTrait =>
        {
          // prepare users
          prepareUser(testUser1)
          prepareUser(testUser2)
          prepareUser(testUser3)

          // prepare data
          val mat1 = TestDataMatriculation.validMatriculationDataCustom(testUser1, examReg1)
          val mat2 = TestDataMatriculation.validMatriculationDataCustom(testUser2, examReg2)
          val mat3 = TestDataMatriculation.validMatriculationDataCustom(testUser3, examReg1)

          // approve as Users
          initializeOperation(testUser1).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", mat1)
          initializeOperation(testUser2).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", mat2)
          initializeOperation(testUser3).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", mat3)

          // store on chain
          TestHelper.trySetupConnections(
            "setupMatriculations",
            () => { matConnection.addMatriculationData(mat1) },
            () => { matConnection.addMatriculationData(mat2) },
            () => { matConnection.addMatriculationData(mat3) }
          )
        }
    }
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    // TODO: RESET LEDGER
    super.afterAll()
  }

  "The ScalaAPI for Admissions" when {
    "invoked with addAdmission correctly " should {
      "allow for adding new Admission with admissionId" in {
        val testData = TestDataAdmission.courseAdmission1(testUser1)
        initializeOperation(testUser1).initiateOperation(testUser1, chaincodeConnection.contractName, "addAdmission", testData)
        TestHelper.testAddAdmissionAccess(chaincodeConnection, testData)
      }
      "allow for adding new Admission without admissionId" in {
        val testData = TestDataAdmission.courseAdmission_noAdmissionId(testUser1)
        initializeOperation(testUser1).initiateOperation(testUser1, chaincodeConnection.contractName, "addAdmission", testData)

        val testResult = chaincodeConnection.addAdmission(testData)
        TestHelper.compareAdmissions(TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1), testResult)
      }
      "allow for adding new Admission for closed ER" in {
        val testData = TestDataAdmission.courseAdmission2(testUser2)
        initializeOperation(testUser2).initiateOperation(testUser2, chaincodeConnection.contractName, "addAdmission", testData)

        TestHelper.testAddAdmissionAccess(chaincodeConnection, testData)
      }
    }

    "invoked with getAdmissions correctly " should {
      val testData: Seq[(String, String, String, String, Seq[String])] = Seq(
        (s"allow for getting all admissions []", "", "", "", Seq(TestDataAdmission.courseAdmission1(testUser1), TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1), TestDataAdmission.courseAdmission2(testUser2))),
        (s"allow for getting all admissions for user [$testUser1]", testUser1, "", "", Seq(TestDataAdmission.courseAdmission1(testUser1), TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1))),
        (s"allow for getting all admissions for user [$testUser2]", testUser2, "", "", Seq(TestDataAdmission.courseAdmission2(testUser2))),
        (s"allow for getting all admissions for course [C.1]", "", "C.1", "", Seq(TestDataAdmission.courseAdmission1(testUser1))),
        (s"allow for getting all admissions for course [C.2]", "", "C.2", "", Seq(TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1), TestDataAdmission.courseAdmission2(testUser2))),
        (s"allow for getting all admissions for module [Admission_Module_1]", "", "", "Admission_Module_1", Seq(TestDataAdmission.courseAdmission1(testUser1), TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1))),
        (s"allow for getting all admissions for module [Admission_Module_2]", "", "", "Admission_Module_2", Seq()),
        (s"allow for getting all admissions for module [Admission_Module_3]", "", "", "Admission_Module_3", Seq(TestDataAdmission.courseAdmission2(testUser2))),
        (s"allow for getting all admissions for user [$testUser1] and course [C.1]", testUser1, "C.1", "", Seq(TestDataAdmission.courseAdmission1(testUser1))),
        (s"allow for getting all admissions for user [$testUser1] and course [C.2]", testUser1, "C.2", "", Seq(TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1))),
        (s"allow for getting all admissions for user [$testUser1] and course [C.3]", testUser1, "C.3", "", Seq()),
        (s"allow for getting all admissions for user [$testUser1] and module [Admission_Module_1]", testUser1, "", "Admission_Module_1", Seq(TestDataAdmission.courseAdmission1(testUser1), TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1))),
        (s"allow for getting all admissions for user [$testUser1] and module [Admission_Module_2]", testUser1, "", "Admission_Module_2", Seq()),
      )
      for ((statement: String, enrollmentId: String, courseId: String, moduleId: String, admissions: Seq[String]) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          val testResult = chaincodeConnection.getCourseAdmissions(enrollmentId, courseId, moduleId)
          val expectedResult = TestHelperStrings.getJsonList(admissions)

          TestHelperStrings.compareJson(expectedResult, testResult)
        }
      }
    }

    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED AFTER THE addAdmission-TESTS.
    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED SEQUENTIALLY IN THIS EXACT ORDER.
    "invoked with dropAdmission correctly " should {
      val testData: Seq[(String, String, String, Seq[String])] = Seq(
        ("allow for dropping existing Admission 1", testUser1, "C.1", Seq(TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1), TestDataAdmission.courseAdmission2(testUser2))),
        ("allow for dropping existing Admission 2", testUser2, "C.2", Seq(TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser1))),
        ("allow for dropping existing Admission 3", testUser1, "C.2", Seq())
      )
      for ((statement: String, userId: String, courseId: String, remainingAdmissions: Seq[String]) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          initializeOperation(userId).initiateOperation(userId, chaincodeConnection.contractName, "dropAdmission", userId + ":" + courseId)
          val testResult = chaincodeConnection.dropAdmission(userId + ":" + courseId)
          testResult should be("")

          // check ledger state
          val ledgerAdmissions = chaincodeConnection.getCourseAdmissions()
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
        val testData = TestDataAdmission.courseAdmission1(testUser3)
        initializeOperation(testUser3).initiateOperation(testUser3, chaincodeConnection.contractName, "addAdmission", testData)
        chaincodeConnection.addAdmission(testData)

        val result = intercept[TransactionExceptionTrait](chaincodeConnection.addAdmission(testData))
        result.transactionName should be("addAdmission")
        // TODO compare errors
        // result.payload should be("")
      }
      "not allow for adding duplicate Admission without admissionId" in {
        // initial Add
        val testData = TestDataAdmission.courseAdmission_noAdmissionId(testUser3)
        initializeOperation(testUser3).initiateOperation(testUser3, chaincodeConnection.contractName, "addAdmission", testData)
        chaincodeConnection.addAdmission(testData)

        val result = intercept[TransactionExceptionTrait](chaincodeConnection.addAdmission(testData))
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
        initializeOperation(testUser3).initiateOperation(testUser3, chaincodeConnection.contractName, "dropAdmission", s"$testUser3:C.1")
        chaincodeConnection.dropAdmission(s"$testUser3:C.1")

        // test exception
        val result = intercept[TransactionExceptionTrait](chaincodeConnection.dropAdmission(s"$testUser3:C.1"))
        result.transactionName should be("dropAdmission")
        // TODO compare errors
        // result.payload should be("")

        // check ledger state
        val ledgerAdmissions = chaincodeConnection.getCourseAdmissions()
        val expectedResult = TestHelperStrings.getJsonList(Seq(TestDataAdmission.courseAdmission_noAdmissionId_WithId(testUser3)))
        TestHelperStrings.compareJson(expectedResult, ledgerAdmissions)
      }
    }
  }
}