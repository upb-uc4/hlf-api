package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData.TestDataMatriculation
import de.upb.cs.uc4.hyperledger.testUtil.{ TestHelper, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class MatriculationErrorTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _
  val existingMatriculationId = "501"

  val mat1 = TestDataMatriculation.validMatriculationData1(existingMatriculationId)

  override def beforeAll(): Unit = {
    super.beforeAll()
    prepareUser(existingMatriculationId)
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())
    TestSetup.establishExistingMatriculation(initializeMatriculation(), initializeOperation(existingMatriculationId), existingMatriculationId, mat1)
  }

  override def beforeEach(): Unit = {
    chaincodeConnection = initializeMatriculation()
  }

  override def afterEach(): Unit = {
    chaincodeConnection.close()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Matriculation" when {
    "invoking getMatriculationData" should {
      val testData: Seq[(String, String)] = Seq(
        ("throw TransactionException for enrollmentId not existing [110]", "110"),
        ("throw TransactionException for enrollmentId empty []", ""),
        ("throw TransactionException for enrollmentId null [null]", null)
      )
      for ((statement: String, data: String) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          TestHelper.testTransactionException("getMatriculationData", () => chaincodeConnection.getMatriculationData(data))
        }
      }
    }

    "invoking addMatriculationData" should {
      val testData: Seq[(String, String)] = Seq(
        ("throw TransactionException for malformed json Input (missing Semester) ", TestDataMatriculation.invalidMatriculationJsonNoSemester("120")),
        ("throw TransactionException for malformed json Input (missing Field of Study) ", TestDataMatriculation.invalidMatriculationJsonNoFieldOfStudy("121")),
        ("throw TransactionException for malformed json Input (missing matriculationStatus) ", TestDataMatriculation.invalidMatriculationJsonNoMatriculationStatus("122")),
        ("throw TransactionException for malformed json Input (missing enrollmentId) ", TestDataMatriculation.invalidMatriculationJsonNoEnrollmentId),
        ("throw TransactionException for malformed json Input (invalid enrollmentId) ", TestDataMatriculation.invalidMatriculationJsonInvalidId),
        ("throw TransactionException for malformed json Input (invalid data 1) ", TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData1("131")),
        ("throw TransactionException for malformed json Input (invalid data 2) ", TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData2("132")),
        ("throw TransactionException for malformed json Input (invalid data 3) ", TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData3("133")),
        ("throw TransactionException for malformed json Input (invalid data 4) ", TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData4("134"))
      )
      for ((testDescription: String, matriculationData: String) <- testData) {
        s"$testDescription" in {
          Logger.info("Begin test: " + testDescription)
          TestHelper.testTransactionException(
            "addMatriculationData",
            () => chaincodeConnection.addMatriculationData(matriculationData)
          )
        }
      }
    }

    "invoking addEntriesToMatriculationData" should {
      val testData: Seq[(String, String, String, String)] = Seq(
        ("throw TransactionException for enrollmentId not existing", "140", "ComputerScience", "SS2020"),
        ("throw TransactionException for enrollmentId empty", "", "ComputerScience", "SS2020"),
        ("throw TransactionException for enrollmentId null", "", "ComputerScience", "SS2020"),
        ("throw TransactionException for semester Entry malformed", existingMatriculationId, "ComputerScience", "S2020"),
        ("throw TransactionException for semester Entry empty", existingMatriculationId, "ComputerScience", ""),
        ("throw TransactionException for semester Entry null", existingMatriculationId, "ComputerScience", ""),
        ("throw TransactionException for fieldOfStudy Entry empty", existingMatriculationId, "", "SS2020"),
        ("throw TransactionException for fieldOfStudy Entry null", existingMatriculationId, null, "SS2020"),
      )
      for ((statement: String, id: String, fieldOfStudy: String, semester: String) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          TestHelper.testTransactionException(
            "addEntriesToMatriculationData",
            () => chaincodeConnection.addEntriesToMatriculationData(
              id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
            )
          )
        }
      }
      "throw TransactionException for super empty matriculationList " in {
        val id = "001"
        TestHelper.testTransactionException(
          "addEntriesToMatriculationData",
          () => chaincodeConnection.addEntriesToMatriculationData(id, "[]")
        )
      }
    }
  }
}
