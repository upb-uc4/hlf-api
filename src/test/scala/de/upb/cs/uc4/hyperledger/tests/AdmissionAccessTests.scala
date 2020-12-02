package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataAdmission, TestDataExaminationRegulation, TestDataMatriculation, TestHelper }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class AdmissionAccessTests extends TestBase {

  var chaincodeConnection: ConnectionAdmissionTrait = _

  val admission1: String = TestDataAdmission.validAdmission("AdmissionStudent_1", "C.1", "AdmissionModule_1", "2020-12-31T23:59:59")
  val admission2: String = TestDataAdmission.validAdmission("AdmissionStudent_2", "C.2", "AdmissionModule_3", "2020-12-31T23:59:59")
  val admission_noAdmissionId: String = TestDataAdmission.validAdmissionNoAdmissionId("AdmissionStudent_1", "C.2", "AdmissionModule_1", "2020-12-31T23:59:59")
  val admission_noAdmissionId_WithId: String = TestDataAdmission.validAdmission("AdmissionStudent_1", "C.2", "AdmissionModule_1", "2020-12-31T23:59:59")

  override def beforeAll(): Unit = {
    super.beforeAll()
    // TODO: RESET LEDGER
    setupExaminationRegulations()
    setupMatriculations()
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
        TestHelper.testAddAdmissionAccess(chaincodeConnection, admission1)
      }
      "allow for adding new Admission for closed ER" in {
        TestHelper.testAddAdmissionAccess(chaincodeConnection, admission2)
      }
      "allow for adding new Admission without admissionId" in {
        val testResult = chaincodeConnection.addAdmission(admission_noAdmissionId)
        TestHelper.compareAdmissions(admission_noAdmissionId_WithId, testResult)
      }
    }

    "invoked with getAdmissions correctly " should {
      val testData: Seq[(String, String, String, String, Seq[String])] = Seq(
        ("allow for getting all admissions []", "", "", "", Seq(admission1, admission2, admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for user [AdmissionStudent_1]", "AdmissionStudent_1", "", "", Seq(admission1, admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for user [AdmissionStudent_2]", "AdmissionStudent_2", "", "", Seq(admission2)),
        ("allow for getting all admissions for course [C.1]", "", "C.1", "", Seq(admission1)),
        ("allow for getting all admissions for course [C.2]", "", "C.2", "", Seq(admission2, admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for module [AdmissionModule_1]", "", "", "AdmissionModule_1", Seq(admission1, admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for module [AdmissionModule_2]", "", "", "AdmissionModule_2", Seq()),
        ("allow for getting all admissions for module [AdmissionModule_3]", "", "", "AdmissionModule_3", Seq(admission2)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and course [C.1]", "AdmissionStudent_1", "C.1", "", Seq(admission1)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and course [C.2]", "AdmissionStudent_1", "C.2", "", Seq(admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and course [C.3]", "AdmissionStudent_1", "C.3", "", Seq()),
        ("allow for getting all admissions for user [AdmissionStudent_1] and module [AdmissionModule_1]", "AdmissionStudent_1", "", "AdmissionModule_1", Seq(admission1, admission_noAdmissionId_WithId)),
        ("allow for getting all admissions for user [AdmissionStudent_1] and module [AdmissionModule_2]", "AdmissionStudent_1", "", "AdmissionModule_2", Seq()),
      )
      for ((statement: String, enrollmentId: String, courseId: String, moduleId: String, admissions: Seq[String]) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          val testResult = chaincodeConnection.getAdmissions(enrollmentId, courseId, moduleId)
          val expectedResult = TestHelper.getJsonList(admissions)

          TestHelper.compareJson(expectedResult, testResult)
        }
      }
    }

    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED AFTER THE addAdmission-TESTS.
    // IMPORTANT: THESE TESTS HAVE TO BE EXECUTED SEQUENTIALLY IN THIS EXACT ORDER.
    "invoked with dropAdmission correctly " should {
      "allow for dropping existing Admission 1 " in {
        val testResult = chaincodeConnection.dropAdmission("AdmissionStudent_1:C.1")
        testResult should be("")

        // check ledger state
        val ledgerAdmissions = chaincodeConnection.getAdmissions()
        val expectedResult = TestHelper.getJsonList(Seq(admission2, admission_noAdmissionId_WithId))
        TestHelper.compareJson(expectedResult, ledgerAdmissions)
      }
      "allow for dropping existing Admission 2 " in {
        val testResult = chaincodeConnection.dropAdmission("AdmissionStudent_2:C.2")
        testResult should be("")

        // check ledger state
        val ledgerAdmissions = chaincodeConnection.getAdmissions()
        val expectedResult = TestHelper.getJsonList(Seq(admission_noAdmissionId_WithId))
        TestHelper.compareJson(expectedResult, ledgerAdmissions)
      }
      "allow for dropping existing Admission 3 " in {
        val testResult = chaincodeConnection.dropAdmission("AdmissionStudent_1:C.2")
        testResult should be("")

        // check ledger state
        val ledgerAdmissions = chaincodeConnection.getAdmissions()
        val expectedResult = TestHelper.getJsonList(Seq())
        TestHelper.compareJson(expectedResult, ledgerAdmissions)
      }
    }
  }

  def setupExaminationRegulations(): Unit = {
    val erConnection = initializeExaminationRegulation()

    // prepare data
    val modules1 = Seq(TestDataExaminationRegulation.getModule("AdmissionModule_1"), TestDataExaminationRegulation.getModule("AdmissionModule_2"))
    val modules2 = Seq(TestDataExaminationRegulation.getModule("AdmissionModule_3"), TestDataExaminationRegulation.getModule("AdmissionModule_4"))
    val openER = TestDataExaminationRegulation.validExaminationRegulation("AdmissionER_Open1", modules1, state = true)
    val closedER = TestDataExaminationRegulation.validExaminationRegulation("AdmissionER_Closed1", modules2, state = false)

    // store on chain
    try {
      erConnection.addExaminationRegulation(openER)
      erConnection.addExaminationRegulation(closedER)
    }
    catch {
      case e: Throwable => throw Logger.err("Error during setupExaminationRegulations", e)
    }
  }

  def setupMatriculations(): Unit = {
    val matConnection = initializeMatriculation()

    // prepare data
    val mat1 = TestDataMatriculation.validMatriculationDataCustom("AdmissionStudent_1", "AdmissionER_Open1")
    val mat2 = TestDataMatriculation.validMatriculationDataCustom("AdmissionStudent_2", "AdmissionER_Closed1")

    // store on chain
    try {
      matConnection.addMatriculationData(mat1)
      matConnection.addMatriculationData(mat2)
    }
    catch {
      case e: Throwable => throw Logger.err("Error during setupMatriculations", e)
    }
  }
}