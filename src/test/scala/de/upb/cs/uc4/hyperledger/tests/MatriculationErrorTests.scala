package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.TestDataMatriculation
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class MatriculationErrorTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _
  val existingMatriculationId = "501"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val examinationRegulationConnection = initializeExaminationRegulation()
    TestDataMatriculation.establishExaminationRegulations(initializeExaminationRegulation())
    examinationRegulationConnection.close()

    try {
      chaincodeConnection = initializeMatriculation()
      chaincodeConnection.addMatriculationData(TestDataMatriculation.validMatriculationData1(existingMatriculationId))
      chaincodeConnection.close()
    }
    catch {
      case e: Exception => Logger.err("[MatriculationErrorTests] :: ", e)
    }
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

  private def testTransactionException(transactionName: String, f: () => Any) = {
    val result = intercept[TransactionExceptionTrait](f.apply())
    result.transactionName should be(transactionName)
  }

  "The ScalaAPI for Matriculation" when {
    "invoking getMatriculationData" should {
      "throw TransactionException for not existing enrollmentId " in {
        testTransactionException("getMatriculationData", () => chaincodeConnection.getMatriculationData("110"))
      }
      "throw TransactionException for empty enrollmentId-String " in {
        testTransactionException("getMatriculationData", () => chaincodeConnection.getMatriculationData(""))
      }
    }

    "invoking addMatriculationData" should {
      "throw TransactionException for malformed json Input (missing Semester) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoSemester("120"))
        )
      }
      "throw TransactionException for malformed json Input (missing Field of Study) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoFieldOfStudy("121"))
        )
      }
      "throw TransactionException for malformed json Input (missing matriculationStatus) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationStatus("122"))
        )
      }
      "throw TransactionException for malformed json Input (missing enrollmentId) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationId)
        )
      }
      "throw TransactionException for malformed json Input (invalid enrollmentId) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidId)
        )
      }
      "throw TransactionException for malformed json Input (invalid data 1) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData1("131"))
        )
      }
      "throw TransactionException for malformed json Input (invalid data 2) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData2("132"))
        )
      }
      "throw TransactionException for malformed json Input (invalid data 3) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData3("133"))
        )
      }
      "throw TransactionException for malformed json Input (invalid data 4) " in {
        testTransactionException(
          "addMatriculationData",
          () => chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData4("134"))
        )
      }
    }

    "invoking addEntriesToMatriculationData" should {
      "throw TransactionException for not existing enrollmentId " in {
        val id = "140"
        val fieldOfStudy = "ComputerScience"
        val semester = "SS2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () => chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for empty enrollmentId " in {
        val id = ""
        val fieldOfStudy = "ComputerScience"
        val semester = "SS2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () => chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for malformed semester Entry " in {
        val id = existingMatriculationId
        val fieldOfStudy = "ComputerScience"
        val semester = "S2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () => chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for empty semester Entry " in {
        val id = existingMatriculationId
        val fieldOfStudy = "ComputerScience"
        val semester = ""
        testTransactionException(
          "addEntriesToMatriculationData",
          () => chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for empty fieldOfStudy Entry " in {
        val id = existingMatriculationId
        val fieldOfStudy = ""
        val semester = "SS2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () => chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }

      "throw TransactionException for super empty matriculationList " in {
        val id = "001"
        testTransactionException(
          "addEntriesToMatriculationData",
          () => chaincodeConnection.addEntriesToMatriculationData(id, "[]")
        )
      }
    }
  }
}
