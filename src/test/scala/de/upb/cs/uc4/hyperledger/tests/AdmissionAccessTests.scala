package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataAdmission, TestDataExaminationRegulation, TestDataMatriculation, TestHelper }

class AdmissionAccessTests extends TestBase {

  var chaincodeConnection: ConnectionAdmissionTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()

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
    super.afterAll()
  }

  "The ScalaAPI for Admissions" when {
    "invoked with addAdmission correctly " should {
      "allow for adding new Admission with admissionId" in {
        val student = "AdmissionStudent_1"
        val course = "C.1"
        val module = "AdmissionModule_1"
        val timestamp = "2020-12-31T23:59:59"
        TestHelper.testAddAdmissionAccess(chaincodeConnection, student, course, module, timestamp)
      }
      "allow for adding new Admission without admissionId" in {
        val student = "AdmissionStudent_1"
        val course = "C.2"
        val module = "AdmissionModule_1"
        val timestamp = "2020-12-31T23:59:59"
        val admission = TestDataAdmission.validAdmissionNoAdmissionId(student, course, module, timestamp)
        TestHelper.testAddAdmissionAccess(chaincodeConnection, admission)
      }
      "allow for adding new Admission for closed ER" in {
        val student = "AdmissionStudent_2"
        val course = "C.2"
        val module = "AdmissionModule_3"
        val timestamp = "2020-12-31T23:59:59"
        TestHelper.testAddAdmissionAccess(chaincodeConnection, student, course, module, timestamp)
      }
    }
  }


  def setupExaminationRegulations(): Unit ={
    val erConnection = initializeExaminationRegulation()

    // prepare data
    val modules1 = Seq(TestDataExaminationRegulation.getModule("AdmissionModule_1"), TestDataExaminationRegulation.getModule("AdmissionModule_2"))
    val modules2 = Seq(TestDataExaminationRegulation.getModule("AdmissionModule_3"), TestDataExaminationRegulation.getModule("AdmissionModule_4"))
    val openER = TestDataExaminationRegulation.validExaminationRegulation("AdmissionER_Open1", modules1, state = true)
    val closedER =TestDataExaminationRegulation.validExaminationRegulation("AdmissionER_Closed1", modules2, state = false)

    // store on chain
    erConnection.addExaminationRegulation(openER)
    erConnection.addExaminationRegulation(closedER)
  }

  def setupMatriculations(): Unit ={
    val matConnection = initializeMatriculation()

    // prepare data
    val mat1 = TestDataMatriculation.validMatriculationDataCustom("AdmissionStudent_1", "AdmissionER_Open1")
    val mat2 = TestDataMatriculation.validMatriculationDataCustom("AdmissionStudent_2", "AdmissionER_Closed1")

    // store on chain
    matConnection.addMatriculationData(mat1)
    matConnection.addMatriculationData(mat2)
  }
}