package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExamTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil._
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class ExamTests extends TestBase {

  var chaincodeConnection: ConnectionExamTrait = _

  val testNamePrefix = "ExamTest"

  val testModule1 = testNamePrefix + "_Module_1"
  val testModule2 = testNamePrefix + "_Module_2"
  val testModule3 = testNamePrefix + "_Module_3"
  val testModule4 = testNamePrefix + "_Module_4"
  val testUser1 = "lecturer_1"
  val testUser2 = "lecturer_2"
  val testUser3 = "admin_1"
  val testUser4 = "admin_2"
  val system: String = username

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeExam()
    // TODO: RESET LEDGER
    TestSetup.setupExaminationRegulations(initializeExaminationRegulation(), testNamePrefix)
    prepareUser(testUser1)
    prepareUser(testUser2)
    prepareUser(testUser3)
    prepareUser(testUser4)
    TestSetup.establishGroups(initializeGroup(), testUser1, TestDataGroup.lecturerGroupName)
    TestSetup.establishGroups(initializeGroup(), testUser2, TestDataGroup.lecturerGroupName)
    TestSetup.establishGroups(initializeGroup(), testUser3, TestDataGroup.adminGroupName)
    TestSetup.establishGroups(initializeGroup(), testUser4, TestDataGroup.adminGroupName)
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    // TODO: RESET LEDGER
    super.afterAll()
  }

  "The ScalaAPI for Exams" when {
    "invoked with addExam correctly " should {
      val testData: Seq[(String, String, String, String, String, Int, String, String, String)] = Seq(
        ("allow for adding new Exam", "C.1", testUser1, testModule1, "Written Exam", 6, "2021-03-12T12:00:00", "2021-03-12T12:00:00", "2021-03-12T12:00:00"),
        ("allow for adding new Exam", "C.1", testUser1, testModule1, "Written Exam", 2, "2021-03-12T12:00:00", "2021-03-12T12:00:00", "2021-03-12T12:00:00"),
        ("allow for adding new Exam", "C.1", testUser1, testModule1, "Written Exam", 0, "2021-03-12T12:00:00", "2021-03-12T12:00:00", "2021-03-12T12:00:00"),
        ("allow for adding new Exam", "C.1", testUser1, testModule1, "Written Exam", 99, "2021-03-12T12:00:00", "2021-03-12T12:00:00", "2021-03-12T12:00:00"),
        ("allow for adding new Exam", "C.1", testUser1, testModule1, "Written Exam", 6, "2021-03-11T12:00:00", "2021-03-12T12:00:00", "2021-03-12T12:00:00"),
        ("allow for adding new Exam", "C.1", testUser1, testModule2, "Written Exam", 6, "2021-03-10T12:00:00", "2021-02-12T12:00:00", "2021-03-12T12:00:00"),
        ("allow for adding new Exam", "C.1", testUser1, testModule3, "Written Exam", 6, "2021-03-09T12:00:00", "2021-01-12T12:00:00", "2021-03-12T12:00:00"),
        ("allow for adding new Exam", "C.1", testUser1, testModule4, "Written Exam", 6, "2021-03-08T12:00:00", "2021-00-12T12:00:00", "2021-04-12T12:00:00")
      )
      for ((statement: String, courseId: String, lecturerId: String, moduleId: String, examType: String, ects: Int, date: String, admitUntil: String, dropUntil: String) <- testData) {
        s"$statement" in {
          Logger.info(s"Begin test: $statement with [$courseId, $lecturerId, $moduleId, $examType, $ects]")
          val testExam = TestDataExam.validExam(courseId, lecturerId, moduleId, examType, date, ects, admitUntil, dropUntil)
          val testResult = chaincodeConnection.addExam(testExam)
          val expectedResult = testExam

          TestHelperStrings.compareJson(expectedResult, testResult)
        }
      }
    }
  }
}