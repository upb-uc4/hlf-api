package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData.TestDataMatriculation
import de.upb.cs.uc4.hyperledger.testUtil.{ TestHelperStrings, TestSetup }

class MatriculationAccessTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _

  val testUserId: String = "200"

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())

    // prepare testUser
    prepareUser(testUserId)
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
    "invoked with correct transactions " should {
      "allow for adding new MatriculationData / Students " in {
        val newData = TestDataMatriculation.validMatriculationData1(testUserId)

        // approve as new user
        initializeOperation(testUserId).initiateOperation(testUserId, "UC4.MatriculationData", "addMatriculationData", newData)

        // add matriculation as admin
        TestHelperStrings.compareJson(newData, chaincodeConnection.addMatriculationData(newData))
      }
      "allow for reading MatriculationData / Students " in {
        chaincodeConnection.getMatriculationData("200")
      }
      "read the correct data " in {
        val newData = TestDataMatriculation.validMatriculationData1("200")
        TestHelperStrings.compareJson(newData, chaincodeConnection.getMatriculationData("200"))
      }
      "allow for adding new Entries to existing data " in {
        val enrollmentId = "200"
        val newEntry = TestDataMatriculation.getSubjectMatriculationList("Computer Science", "SS2021")

        // approvals
        initializeOperation(testUserId).initiateOperation(testUserId, "UC4.MatriculationData", "addEntriesToMatriculationData", enrollmentId, newEntry)

        // actual operation
        val result = chaincodeConnection.addEntriesToMatriculationData(enrollmentId, newEntry)
        val expectedResult = chaincodeConnection.getMatriculationData("200")
        TestHelperStrings.compareJson(expectedResult, result)
      }
      "allow for updating existing Data (and thus removing entries) " in {
        val newData = TestDataMatriculation.validMatriculationData2("200")
        TestHelperStrings.compareJson(newData, chaincodeConnection.updateMatriculationData(newData))
        TestHelperStrings.compareJson(newData, chaincodeConnection.getMatriculationData("200"))
      }
    }
  }
}