package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCourseTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData.TestDataCourses

class CourseErrorTests extends TestBase {

  var chaincodeConnection: ConnectionCourseTrait = _

  override def beforeEach(): Unit = {
    chaincodeConnection = initializeCourses()
  }

  override def afterEach(): Unit = {
    chaincodeConnection.close()
  }

  "The ScalaAPI for Courses" when {
    "invoking getCourseById" should {
      "throw TransactionErrorException for non existent courseId " in {
        // test action
        val result = intercept[TransactionExceptionTrait](() -> chaincodeConnection.getCourseById("0"))
        result.transactionName should be("getCourseById")
      }
    }
    "invoking updateCourseById" should {
      "throw TransactionErrorException for non existent courseId " in {
        // test action
        val result = intercept[TransactionExceptionTrait](() -> chaincodeConnection.updateCourseById("0", TestDataCourses.invalidCourseData(null)))
        result.transactionName should be("updateCourseById")
      }
    }
  }
}