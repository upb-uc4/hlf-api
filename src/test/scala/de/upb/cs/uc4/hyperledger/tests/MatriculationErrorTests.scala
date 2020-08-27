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
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.getMatriculationData("110"))
        result.transactionId should ===("getMatriculationData")
      }
      "throw TransactionException for empty matriculationId-String " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.getMatriculationData(""))
        result.transactionId should ===("getMatriculationData")
      }
    }

    "invoking addMatriculationData" should {
      "throw TransactionException for malformed json Input (missing Semester) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoSemester("120")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing Field of Study) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoFieldOfStudy("121")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing matriculationStatus) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationStatus("122")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing MatId) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoMatriculationId("123")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing FirstName) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoFirstName("124")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing LastName) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoLastName("125")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (missing Birthdate) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonNoBirthdate("126")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid Birthdate) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidBirthdate("130")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid id) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidId))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 1) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData1("131")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 2) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData2("132")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 3) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData3("133")))
        result.transactionId should ===("addMatriculationData")
      }

      "throw TransactionException for malformed json Input (invalid data 4) " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.addMatriculationData(TestDataMatriculation.invalidMatriculationJsonInvalidMatriculationData4("134")))
        result.transactionId should ===("addMatriculationData")
      }
    }

    "invoking addMatriculationData" should {
      "throw TransactionException for not existing matriculationId " in {
        // test action
        val id="140"
        val fieldOfStudy="ComputerScience"
        val semester="SS2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntryToMatriculationData(id, fieldOfStudy, semester))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for empty matriculationId " in {
        // test action
        val id=""
        val fieldOfStudy="ComputerScience"
        val semester="SS2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntryToMatriculationData(id, fieldOfStudy, semester))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for malformed semester Entry " in {
        // test action
        val id="001"
        val fieldOfStudy="ComputerScience"
        val semester="S2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntryToMatriculationData(id, fieldOfStudy, semester))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for empty semester Entry " in {
        // test action
        val id="001"
        val fieldOfStudy="ComputerScience"
        val semester=""
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntryToMatriculationData(id, fieldOfStudy, semester))
        result.transactionId should ===("addEntryToMatriculationData")
      }
      "throw TransactionException for empty fieldOfStudy Entry " in {
        // test action
        val id="001"
        val fieldOfStudy=""
        val semester="SS2020"
        val result = intercept[TransactionException](() -> chaincodeConnection.addEntryToMatriculationData(id, fieldOfStudy, semester))
        result.transactionId should ===("addEntryToMatriculationData")
      }
    }
  }
}