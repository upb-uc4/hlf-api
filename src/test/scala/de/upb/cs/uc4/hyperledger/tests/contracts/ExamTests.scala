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
      val testData: Seq[(String, String, String, String, String, Int)] = Seq(
        ("allow for adding new Exam (Approvals forged)", "C.1", testUser1, testModule1, "Written Exam", 6),
        ("allow for adding new Exam (Approvals forged)", "C.1", testUser1, testModule1, "Written Exam", 2),
        ("allow for adding new Exam (Approvals forged)", "C.1", testUser1, testModule1, "Written Exam", 0),
        ("allow for adding new Exam (Approvals forged)", "C.1", testUser1, testModule1, "Oral Exam", 99),
        ("allow for adding new Exam (Approvals forged)", "C.1", testUser1, testModule1, "Oral Exam", 0),
        ("allow for adding new Exam (Approvals forged)", "C.1", testUser1, testModule1, "Garbage", 0)
      )
      for ((statement: String, courseId: String, lecturerId: String, moduleId: String, examType: String, ects: Int) <- testData) {
        s"$statement" in {
          Logger.info(s"Begin test: $statement with [$courseId, $lecturerId, $moduleId, $examType, $ects]")

          // prepare data
          val testExam = TestDataExam.validFutureExam(courseId, lecturerId, moduleId, examType, ects)

          // forge approval (lecturer)
          initializeOperation(lecturerId).initiateOperation(lecturerId, "UC4.Exam", "addExam", testExam)

          // implicit approval (system, admin)
          val testResult = chaincodeConnection.addExam(testExam)

          // test
          val expectedResult = testExam
          TestHelperStrings.compareJson(expectedResult, testResult)
        }
      }
    }

    "invoked with getExams correctly " should {
      val testData: Seq[(String, Seq[String], Seq[String], Seq[String], Seq[String], Seq[String], String, String)] = Seq(
        ("allow for getting Exams", Seq(), Seq(), Seq(), Seq(), Seq(), "", ""),
        ("allow for getting Exams", Seq(), Seq(), Seq(), Seq(), Seq(), "", ""),
        ("allow for getting Exams", Seq(), Seq(), Seq(), Seq(), Seq(), "", "")
      )
      for (
        (statement: String, examIds: Seq[String], courseIds: Seq[String], lecturerIds: Seq[String], moduleIds: Seq[String],
          types: Seq[String], admittableAt: String, droppableAt: String) <- testData
      ) {
        s"$statement" in {
          Logger.info(s"Begin test: $statement with [$examIds, $courseIds, $lecturerIds, $moduleIds, $types, $admittableAt, $droppableAt]")

          // should not throw exception
          val testResult = chaincodeConnection.getExams(examIds.toList, courseIds.toList, lecturerIds.toList, moduleIds.toList,
            types.toList, admittableAt, droppableAt)

          val expectedResult = ""
          // TestHelperStrings.compareJson(expectedResult, testResult)
        }
      }
    }
  }
}