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
        val result = chaincodeConnection.addMatriculationData(TestDataMatriculation.validMatriculationData1("200"))
        result should ===("")
      }
      "allow for reading MatriculationData / Students " in {
        executeAndLog(() => { chaincodeConnection.getMatriculationData("200") })
      }
      "read the correct data " in {
        val result = chaincodeConnection.getMatriculationData("200")
        TestHelper.compareJson(TestDataMatriculation.validMatriculationData1("200"), result)
      }
      "allow for adding new Entries to existing data " in {
        executeAndLog(() => {
          val result = chaincodeConnection.addEntriesToMatriculationData("200", TestDataMatriculation.getSubjectMatriculationList("ComputerScience", "SS2021"))
          result should ===("")
        })
      }
      "allow for updating existing Data (and thus removing entries) " in {
        val result = chaincodeConnection.updateMatriculationData(TestDataMatriculation.validMatriculationData1("200"))
        result should ===("")
        val readData = chaincodeConnection.getMatriculationData("200")
        TestHelper.compareJson(TestDataMatriculation.validMatriculationData1("200"), readData)
      }
    }
  }
}