import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.testBase.TestBase

class MatriculationErrorTests extends TestBase {

  var chaincodeConnection: ConnectionMatriculationTrait = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    chaincodeConnection = initializeMatriculation()
  }

  override def afterEach(): Unit = {
    super.afterEach()
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