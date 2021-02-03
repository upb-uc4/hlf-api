package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExamTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil._
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class ExamTests extends TestBase {

  val chaincodeConnection: ConnectionExamTrait = initializeExam()

  val testNamePrefix = "ExamTest"

  val testModule1: String = testNamePrefix + "_Module_1"
  val testModule2: String = testNamePrefix + "_Module_2"
  val testModule3: String = testNamePrefix + "_Module_3"
  val testModule4: String = testNamePrefix + "_Module_4"
  val testUser1 = "lecturer_1"
  val testUser2 = "lecturer_2"
  val testUser3 = "admin_1"
  val testUser4 = "admin_2"
  val system: String = username

  override def beforeAll(): Unit = {
    super.beforeAll()
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
      val testDataAllow: Seq[(String, String, String, String, String, Int)] = Seq(
        ("allow for adding new valid Exam (Approvals forged)", "C.1", testUser1, testModule1, "Written Exam", 6),
        ("allow for adding new valid Exam (Approvals forged)", "C.1", testUser1, testModule1, "Written Exam", 2),
        ("allow for adding new valid Exam (Approvals forged)", "C.1", testUser1, testModule1, "Written Exam", 0),
        ("allow for adding new valid Exam (Approvals forged)", "C.1", testUser1, testModule1, "Oral Exam", 99),
        ("allow for adding new valid Exam (Approvals forged)", "C.1", testUser1, testModule1, "Oral Exam", 0),
        ("allow for adding new valid Exam (Approvals forged)", "C.1", testUser1, testModule1, "Garbage", 0)
      )
      for ((statement: String, courseId: String, lecturerId: String, moduleId: String, examType: String, ects: Int) <- testDataAllow) {
        s"$statement [$courseId, $lecturerId, $moduleId, $examType, $ects]" in {
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

      val testDataDeny: Seq[(String, String, String, String, String, String, Int, String, String, String)] = Seq(
        ("deny adding invalid Exam", s"Garbage", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1+1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2+1:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1+1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam+1:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:01", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1+1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser1, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1 + 1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam+1", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6 + 1, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:01", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", "", testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, "", "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", null, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"), null case not possible
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", ""),
        ("deny adding invalid Exam", s"C.1:Garbage:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", "Garbage", testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:Garbage:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, "Garbage", "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Garbage:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Garbage", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:0:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 0, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2020-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2020-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2020-03-10T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2020-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-15T12:00:00", "2021-03-12T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-15T12:00:00"),
        ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-08T12:00:00"),
      )
      for ((statement: String, examId: String, courseId: String, lecturerId: String, moduleId: String, examType: String, ects: Int, date: String, admitUntil: String, dropUntil: String) <- testDataDeny) {
        s"directly $statement [$examId, $courseId, $lecturerId, $moduleId, $examType, $ects, $date, $admitUntil, $dropUntil]" in {
          Logger.info(s"Begin test: $statement with [$examId, $courseId, $lecturerId, $moduleId, $examType, $ects, $date, $admitUntil, $dropUntil]")

          // prepare data
          val testExam = TestDataExam.customizableExam(examId, courseId, lecturerId, moduleId, examType, date, ects, admitUntil, dropUntil)

          // implicit approval
          val result = intercept[TransactionExceptionTrait](chaincodeConnection.addExam(testExam))
          Logger.debug(s"AddInvalidExamTest: $result")

          // test
          // TODO: check specific error
        }
        s"indirectly $statement [$examId, $courseId, $lecturerId, $moduleId, $examType, $ects, $date, $admitUntil, $dropUntil]" in {
          Logger.info(s"Begin test: $statement with [$examId, $courseId, $lecturerId, $moduleId, $examType, $ects, $date, $admitUntil, $dropUntil]")

          // prepare data
          val testExam = TestDataExam.customizableExam(examId, courseId, lecturerId, moduleId, examType, date, ects, admitUntil, dropUntil)

          // forge approval (lecturer)
          val result = intercept[TransactionExceptionTrait](Logger.debug("initOpResultTest: " + initializeOperation(lecturerId).initiateOperation(lecturerId, "UC4.Exam", "addExam", testExam)))
          Logger.debug(s"AddInvalidExamTest: $result")

          // test
          // TODO: check specific error
        }
      }
    }

    "invoked with getExams correctly " should {
      val allExams = chaincodeConnection.getExams(Seq().toList, Seq().toList, Seq().toList, Seq().toList, Seq().toList, "", "")
      Logger.debug(s"All Exams: $allExams")

      val testData: Seq[(String, Seq[String], Seq[String], Seq[String], Seq[String], Seq[String], String, String)] = Seq(
        ("allow for getting Exams", Seq(), Seq(), Seq(), Seq(), Seq(), "", ""),
        ("allow for getting Exams2", Seq(), Seq(), Seq(), Seq(), Seq(), "", ""),
        ("allow for getting Exams3", Seq(), Seq(), Seq(), Seq(), Seq(), "", "")
      )
      for (
        (statement: String, examIds: Seq[String], courseIds: Seq[String], lecturerIds: Seq[String], moduleIds: Seq[String],
          types: Seq[String], admittableAt: String, droppableAt: String) <- testData
      ) {
        s"$statement [$examIds, $courseIds, $lecturerIds, $moduleIds, $types, $admittableAt, $droppableAt]" in {
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