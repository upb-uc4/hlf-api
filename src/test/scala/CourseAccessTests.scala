import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCourseTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData.TestDataCourses

class CourseAccessTests extends TestBase {

  var chaincodeConnection: ConnectionCourseTrait = _

  override def beforeEach(): Unit = {
    chaincodeConnection = initializeCourses()
  }

  override def afterEach(): Unit = {
    chaincodeConnection.close()
  }

  "A ChaincodeConnection" when {
    "accessed as expected" should {

      "allow for getAllCourses" in {
        //retrieve result on query
        val courses = chaincodeConnection.getAllCourses

        // test result
        courses should not be null
      }

      "allow a full walk-through" in {
        // initial courses
        val getAllCourses = chaincodeConnection.getAllCourses
        getAllCourses should not be null
        println("Courses: " + getAllCourses)

        // add new course
        val testCourseId = "41"
        val addCourseResult = chaincodeConnection.addCourse(TestDataCourses.exampleCourseData(testCourseId))
        addCourseResult should not be null
        addCourseResult should equal("")
        println("AddNew Result: " + addCourseResult)

        // Check AddNew worked as expected READ COURSE
        val readCourseResult = chaincodeConnection.getCourseById(testCourseId)
        readCourseResult should not be null
        println("newCourse read: " + readCourseResult)
        println("example data: " + TestDataCourses.exampleCourseData(testCourseId))
        readCourseResult should equal(TestDataCourses.exampleCourseData(testCourseId))

        // delete new course
        val deleteCourseResult = chaincodeConnection.deleteCourseById(testCourseId)
        deleteCourseResult should not equal null
        println("deleteCourseResult: " + deleteCourseResult)

        // access deleted course shall throw an exception
        intercept[TransactionException](() -> chaincodeConnection.getCourseById(testCourseId))

        // update new course
        // add new course
        val testUpdateCourseId = "90"
        val updateAddCourseResult = chaincodeConnection.addCourse(TestDataCourses.exampleCourseData(testUpdateCourseId))
        updateAddCourseResult should equal("")
        // update
        val updateCoursesResult = chaincodeConnection.updateCourseById(testUpdateCourseId, TestDataCourses.exampleCourseData2(testUpdateCourseId))
        updateCoursesResult should not be null
        updateCoursesResult should equal("")
      }
    }
  }
}