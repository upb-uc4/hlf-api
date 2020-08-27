package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
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
        val result = chaincodeConnection.getMatriculationData("200")
      }
      "read the correct data " in {
        val result = chaincodeConnection.getMatriculationData("200")
        result should ===(TestDataMatriculation.validMatriculationData1("200"))
      }
      "allow for adding new Entries to existing data " in {
        val result = chaincodeConnection.addEntryToMatriculationData("200", "ComputerScience", "SS2021")
        result should ===("")
      }
      "allow for updating existing Data (and thus removing entries) " in {
        val result = chaincodeConnection.updateMatriculationData(TestDataMatriculation.validMatriculationData1("200"))
        result should ===("")
        val readData = chaincodeConnection.getMatriculationData("200")
        readData should ===(TestDataMatriculation.validMatriculationData1("200"))
      }
    }
  }
}