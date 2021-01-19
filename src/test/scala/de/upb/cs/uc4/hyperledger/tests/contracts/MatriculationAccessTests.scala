package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.{ TestDataMatriculation, TestHelperStrings, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager }

class MatriculationAccessTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestSetup.establishAdminAndSystemGroup(initializeGroup(), username)
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())
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
        val newData = TestDataMatriculation.validMatriculationData1("200")

        // approve as new user
        val enrollmentID = "200"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, enrollmentID, username, walletPath, "org1")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, enrollmentID, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)
        initializeOperation(enrollmentID).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", newData)
        initializeOperation(username).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", newData)

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
        val result = chaincodeConnection.addEntriesToMatriculationData(
          "200",
          TestDataMatriculation.getSubjectMatriculationList("Computer Science", "SS2021")
        )
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