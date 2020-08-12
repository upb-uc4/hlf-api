
import java.nio.file.Paths

import de.upb.cs.uc4.hyperledger.ConnectionManager
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.exceptions.{TransactionErrorException, TransactionException}
import de.upb.cs.uc4.hyperledger.traits.ChaincodeActionsTrait
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MatriculationErrorTests extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  val connectionManager = ConnectionManager(
    Paths.get(getClass.getResource("/connection_profile.yaml").toURI),
    Paths.get(getClass.getResource("/wallet/").toURI))
  var chaincodeConnection: ChaincodeActionsTrait = null

  override def beforeEach() {
    chaincodeConnection = connectionManager.createConnection()
  }

  override def afterEach() {
    chaincodeConnection.close()
  }


  "The ScalaAPI for Matriculation" when {
    "Provoking TransactionExceptions" should {
      "throw TransactionException for not existing matriculationId " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.evaluateTransaction("getMatriculationData", "1"))
        result.transactionId should ===("getMatriculationData")
        println(result.jsonError)
      }
    }
  }
}