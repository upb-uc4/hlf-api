package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCourseTrait
import de.upb.cs.uc4.hyperledger.testData.TestDataCourses
import org.scalatest.matchers.should.Matchers.be
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

object TestHelper {

  def testChaincodeCourseAccess(testId: String, courseConnection: ConnectionCourseTrait): Unit = {
    val testCourse = TestDataCourses.exampleCourseData(testId)
    courseConnection.addCourse(testCourse)
    val result = courseConnection.getCourseById(testId)
    TestHelper.compareJson(testCourse, result)
  }

  def compareJson(expected: String, actual: String): Unit = {
    val cleanExpected = expected
      .replace("\n", "")
      .replace(" ", "")
    val cleanActual = actual
      .replace("\n", "")
      .replace(" ", "")
    cleanActual should be(cleanExpected)
  }
}
