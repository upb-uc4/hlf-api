package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataMatriculation, TestHelper, TestSetup }

class MatriculationAccessTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
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
        TestHelper.compareJson(newData, chaincodeConnection.addMatriculationData(newData))
      }
      "allow for reading MatriculationData / Students " in {
        chaincodeConnection.getMatriculationData("200")
      }
      "read the correct data " in {
        val newData = TestDataMatriculation.validMatriculationData1("200")
        TestHelper.compareJson(newData, chaincodeConnection.getMatriculationData("200"))
      }
      "allow for adding new Entries to existing data " in {
        val result = chaincodeConnection.addEntriesToMatriculationData(
          "200",
          TestDataMatriculation.getSubjectMatriculationList("Computer Science", "SS2021")
        )
        val expectedResult = chaincodeConnection.getMatriculationData("200")
        TestHelper.compareJson(expectedResult, result)
      }
      "allow for updating existing Data (and thus removing entries) " in {
        val newData = TestDataMatriculation.validMatriculationData2("200")
        TestHelper.compareJson(newData, chaincodeConnection.updateMatriculationData(newData))
        TestHelper.compareJson(newData, chaincodeConnection.getMatriculationData("200"))
      }
    }
  }
}