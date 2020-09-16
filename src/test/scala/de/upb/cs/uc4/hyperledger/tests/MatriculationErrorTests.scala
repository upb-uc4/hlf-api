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

  "The ScalaAPI for Matriculation" when {
    "invoking getMatriculationData" should {
      "throw TransactionException for not existing matriculationId " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.getMatriculationData("110"))
        result.transactionId should ===("getMatriculationData")
      }
      "throw TransactionException for empty matriculationId-String " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.getMatriculationData(""))
        result.transactionId should ===("getMatriculationData")
      }
    }

    "invoking addMatriculationData" should {
      "throw TransactionException for malformed json Input (missing Semester) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoSemester("120")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing Field of Study) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoFieldOfStudy("121")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing matriculationStatus) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationStatus("122")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing MatId) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationId("123")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing FirstName) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoFirstName("124")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing LastName) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoLastName("125")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing Birthdate) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoBirthdate("126")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid Birthdate) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidBirthdate("130")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid id) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidId))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 1) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData1("131")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 2) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData2("132")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 3) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData3("133")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 4) " in {
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData4("134")))
        result.transactionId should ===("addMatriculationData")
      }
    }

    "invoking addEntriesToMatriculationData" should {
      "throw TransactionException for not existing matriculationId " in {
        val id = "140"
        val fieldOfStudy = "ComputerScience"
        val semester = "SS2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntriesToMatriculationData(
          id,
          TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
        ))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for empty matriculationId " in {
        val id = ""
        val fieldOfStudy = "ComputerScience"
        val semester = "SS2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntriesToMatriculationData(
          id,
          TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
        ))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for malformed semester Entry " in {
        val id = "001"
        val fieldOfStudy = "ComputerScience"
        val semester = "S2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntriesToMatriculationData(
          id,
          TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
        ))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for empty semester Entry " in {
        val id = "001"
        val fieldOfStudy = "ComputerScience"
        val semester = ""
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntriesToMatriculationData(
          id,
          TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
        ))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for empty fieldOfStudy Entry " in {
        val id = "001"
        val fieldOfStudy = ""
        val semester = "SS2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntriesToMatriculationData(
          id,
          TestDataMatriculation.getSubjectMatriculationList(fieldOfStudy, semester)
        ))
        result.transactionId should ===("addEntryToMatriculationData")
      }

      "throw TransactionException for super empty matriculationList " in {
        val id = "001"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntriesToMatriculationData(
          id,
          "[]"
        ))
        result.transactionId should ===("addEntryToMatriculationData")
        println("[DEBUG] :: " + result.toString)
      }
    }
  }
}