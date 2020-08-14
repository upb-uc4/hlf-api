import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.testBase.TestBaseDevNetwork

class MatriculationErrorTests extends TestBaseDevNetwork {

  var chaincodeConnection: ConnectionMatriculationTrait = _

  override def beforeEach(): Unit = {
    chaincodeConnection = initializeMatriculation()
  }

  override def afterEach(): Unit = {
    chaincodeConnection.close()
  }


  "The ScalaAPI for Matriculation" when {
    "Provoking TransactionExceptions" should {
      "throw TransactionException for not existing matriculationId " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.getMatriculationData("1"))
        result.transactionId should ===("getMatriculationData")
        println(result.payload)
      }
    }
  }
}