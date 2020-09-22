package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData.TestDataMatriculation

class MatriculationErrorTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeMatriculation()
    chaincodeConnection.addMatriculationData(TestDataMatriculation.validMatriculationData1("001"))
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  private def testTransactionException(transactionName: String, f: => Any) = {
    val result = intercept[TransactionException](f)
    result.transactionId should be(transactionName)
  }

  "The ScalaAPI for Matriculation" when {
    "invoking getMatriculationData" should {
      "throw TransactionException for not existing matriculationId " in {
        testTransactionException("getMatriculationData", () -> chaincodeConnection.getMatriculationData("110"))
      }
      "throw TransactionException for empty matriculationId-String " in {
        testTransactionException("getMatriculationData", () -> chaincodeConnection.getMatriculationData(""))
      }
    }

    "invoking addMatriculationData" should {
      "throw TransactionException for malformed json Input (missing Semester) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoSemester("120"))
        )
      }
      "throw TransactionException for malformed json Input (missing Field of Study) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoFieldOfStudy("121"))
        )
      }
      "throw TransactionException for malformed json Input (missing matriculationStatus) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationStatus("122"))
        )
      }
      "throw TransactionException for malformed json Input (missing MatId) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationId("123"))
        )
      }
      "throw TransactionException for malformed json Input (missing FirstName) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoFirstName("124"))
        )
      }
      "throw TransactionException for malformed json Input (missing LastName) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoLastName("125"))
        )
      }
      "throw TransactionException for malformed json Input (missing Birthdate) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoBirthdate("126"))
        )
      }
      "throw TransactionException for malformed json Input (invalid Birthdate) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidBirthdate("130"))
        )
      }
      "throw TransactionException for malformed json Input (invalid id) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidId)
        )
      }
      "throw TransactionException for malformed json Input (invalid data 1) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData1("131"))
        )
      }
      "throw TransactionException for malformed json Input (invalid data 2) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData2("132"))
        )
      }
      "throw TransactionException for malformed json Input (invalid data 3) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData3("133"))
        )
      }
      "throw TransactionException for malformed json Input (invalid data 4) " in {
        testTransactionException(
          "addMatriculationData",
          () -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData4("134"))
        )
      }
    }

    "invoking addEntriesToMatriculationData" should {
      "throw TransactionException for not existing matriculationId " in {
        val id = "140"
        val fieldOfStudy = "ComputerScience"
        val semester = "SS2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () -> chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for empty matriculationId " in {
        val id = ""
        val fieldOfStudy = "ComputerScience"
        val semester = "SS2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () -> chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for malformed semester Entry " in {
        val id = "001"
        val fieldOfStudy = "ComputerScience"
        val semester = "S2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () -> chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for empty semester Entry " in {
        val id = "001"
        val fieldOfStudy = "ComputerScience"
        val semester = ""
        testTransactionException(
          "addEntriesToMatriculationData",
          () -> chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
      "throw TransactionException for empty fieldOfStudy Entry " in {
        val id = "001"
        val fieldOfStudy = ""
        val semester = "SS2020"
        testTransactionException(
          "addEntriesToMatriculationData",
          () -> chaincodeConnection.addEntriesToMatriculationData(
            id, TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
          )
        )
      }
    }
  }
}
