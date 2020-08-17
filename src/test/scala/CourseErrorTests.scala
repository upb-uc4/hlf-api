import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCourseTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.testBase.TestBaseDevNetwork
import de.upb.cs.uc4.hyperledger.testData.TestDataCourses

class CourseErrorTests extends TestBaseDevNetwork {

  var chaincodeConnection: ConnectionCourseTrait = _

  override def beforeEach(): Unit = {
    chaincodeConnection = initializeCourses()
  }

  override def afterEach(): Unit = {
    chaincodeConnection.close()
  }


  "The ScalaAPI EvaluateTransaction" when {
    "asked for invalid transactions" should {
      "throw TransactionErrorException for empty transactionId " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.getCourseById("1"))
        result.transactionId should ===("getCourseById")
      }
      "throw TransactionErrorException for wrong transactionId during update " in {
        // test action
        val result = intercept[TransactionException](() -> chaincodeConnection.updateCourseById("1", TestDataCourses.invalidCourseData(null)))
        result.transactionId should ===("updateCourseById")
      }
    }
  }
}