import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCourseTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBaseDevNetwork
import org.scalatest.Succeeded

import scala.util.{Success, Using}

class CourseAccessTests extends TestBaseDevNetwork {

  "A ChaincodeConnection" when {
    "accessed as expected" should {
      "allow for getAllCourses" in {
        // setup connection
        val chaincodeConnection = initializeCourses()

        // perform action
        try {
          //retrieve result on query
          val courses = chaincodeConnection.getAllCourses

          // test result
          courses should not be null
        } finally {
          // close connection
          chaincodeConnection.close()
        }
      }

      "allow a full walkthrough" in {
        val testResult = Using(initializeCourses()) { chaincodeConnection: ConnectionCourseTrait =>
          // initial courses
          val getAllCourses = chaincodeConnection.getAllCourses
          getAllCourses should not be null
          println("Courses: " + getAllCourses)

          // add new course
          val testCourseId = "41"
          val addCourseResult = chaincodeConnection.addCourse(TestData.exampleCourseData(testCourseId))
          addCourseResult should not be null
          addCourseResult should equal("")
          println("AddNew Result: " + addCourseResult)

          // Check AddNew worked as expected READ COURSE
          val readCourseResult = chaincodeConnection.getCourseById(testCourseId)
          readCourseResult should not be null
          println("newCourse read: " + readCourseResult)
          println("example data: " + TestData.exampleCourseData(testCourseId))
          readCourseResult should equal(TestData.exampleCourseData(testCourseId))

          // delete new course
          val deleteCourseResult = chaincodeConnection.deleteCourseById(testCourseId)
          deleteCourseResult should not equal null
          println("deleteCourseResult: " + deleteCourseResult)
          try {
            val tryGetRemovedCourse = chaincodeConnection.getCourseById(testCourseId)
            println("removeResult: " + tryGetRemovedCourse)
            assert(false, "The remove course did not throw an exception when trying to retrieve it.")
          } catch {
            case _: Throwable => println("Correctly threw an exception when getting deleted course.")
          }

          // update new course
          // add new course
          val testUpdateCourseId = "90"
          val updateAddCourseResult = chaincodeConnection.addCourse(TestData.exampleCourseData(testUpdateCourseId))
          updateAddCourseResult should equal("")
          // update
          val updateCouresResult = chaincodeConnection.updateCourseById(testUpdateCourseId, TestData.exampleCourseData2(testUpdateCourseId))
          updateCouresResult should not be null
          updateCouresResult should equal("")
        }

        println("TestResult : " + testResult)
        testResult should equal(Success(Succeeded))
      }
    }
  }
}