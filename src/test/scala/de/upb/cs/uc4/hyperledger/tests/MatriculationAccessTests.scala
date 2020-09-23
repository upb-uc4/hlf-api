package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData.TestDataMatriculation

class MatriculationAccessTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeMatriculation()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Matriculation" when {
    "invoked with correct transactions " should {
      "allow for adding new MatriculationData / Students " in {
        executeAndLog(() => {
          val newData = TestDataMatriculation.validMatriculationData1("200")
          compareJson(newData, chaincodeConnection.addMatriculationData(newData))
        })
      }
      "allow for reading MatriculationData / Students " in {
        executeAndLog(() => {
          chaincodeConnection.getMatriculationData("200")
        })
      }
      "read the correct data " in {
        executeAndLog(() => {
          val newData = TestDataMatriculation.validMatriculationData1("200")
          compareJson(newData, chaincodeConnection.getMatriculationData("200"))
        })
      }
      "allow for adding new Entries to existing data " in {
        executeAndLog(() => {
          val result = chaincodeConnection.addEntriesToMatriculationData(
            "200",
            TestDataMatriculation.getSubjectMatriculationList("Computer Science", "SS2021")
          )
          val expectedResult = chaincodeConnection.getMatriculationData("200")
          compareJson(expectedResult, result)
        })
      }
      "allow for updating existing Data (and thus removing entries) " in {
        executeAndLog(() => {
          val newData = TestDataMatriculation.validMatriculationData2("200")
          compareJson(newData, chaincodeConnection.updateMatriculationData(newData))
          compareJson(newData, chaincodeConnection.getMatriculationData("200"))
        })
      }
    }
  }
}