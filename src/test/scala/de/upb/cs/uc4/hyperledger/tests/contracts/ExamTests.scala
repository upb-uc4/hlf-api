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

  override def beforeAll(): Unit = {
    super.beforeAll()
    // TODO: RESET LEDGER
    TestSetup.setupExaminationRegulations(initializeExaminationRegulation(), testNamePrefix)
    prepareUser(testUser1)
    prepareUser(testUser2)
    TestSetup.establishGroups(initializeGroup(), testUser1, TestDataGroup.lecturerGroupName)
    TestSetup.establishGroups(initializeGroup(), testUser2, TestDataGroup.lecturerGroupName)
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
    }
    "invoked with addExam incorrectly " should {
      val testDataDeny: Seq[(String, String, String, String, String, String, Int, String, String, String, String)] = Seq(
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"Garbage", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1+1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2+1:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1+1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam+1:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:01", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1+1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser1, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1 + 1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam+1", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6 + 1, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:01", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // examId is just reset ("deny adding invalid Exam [ExamId]", s"", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // null case not possible ("deny adding invalid Exam", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", null, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00"),
        // 0 ects allowed ("deny adding invalid Exam [0 ects]", s"C.1:$testUser2:$testModule1:Written Exam:0:2021-03-14T12:00:00", "C.1", testUser2, testModule1, "Written Exam", 0, "2021-03-14T12:00:00", "2021-03-10T12:00:00", "2021-03-12T12:00:00",""),
        ("deny adding invalid Exam [empty CourseId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"exam.courseId\",\"reason\":\"The given parameter must not be empty\"}]}"),
        ("deny adding invalid Exam [empty UserId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", "", testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"Thefollowingparametersdonotconformtothespecifiedformat\",\"invalidParams\":[{\"name\":\"exam.lecturerEnrollmentId\",\"reason\":\"The given parameter must not be empty\"},{\"name\":\"exam.lecturerEnrollmentId\",\"reason\":\"The user trying to add an exam is not registered in the system.\"}]}"),
        ("deny adding invalid Exam [empty ModuleId]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, "", "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"exam.moduleId\",\"reason\":\"The given parameter must not be empty\"}]}"),
        ("deny adding invalid Exam [empty ExamType]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"exam.type\",\"reason\":\"The given parameter must not be empty\"}]}"),
        ("deny adding invalid Exam [empty date]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"exam.date\",\"reason\":\"date must be the following format\\\"(\\\\d{4}-\\\\d{2}-\\\\d{2}T\\\\d{2}:\\\\d{2}:\\\\d{2}.\\\\d{3}Z\\\",e.g.\\\"2020-12-31T23:59:59.000Z\\\"\"}]}"),
        ("deny adding invalid Exam [empty admittableDate]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "", "2021-03-12T12:00:00.000Z",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"exam.admittableUntil\",\"reason\":\"admittableUntil must be the following format\\\"(\\\\d{4}-\\\\d{2}-\\\\d{2}T\\\\d{2}:\\\\d{2}:\\\\d{2}.\\\\d{3}Z\\\",e.g.\\\"2020-12-31T23:59:59.000Z\\\"\"}]}"),
        ("deny adding invalid Exam [empty droppableDate]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"exam.droppableUntil\",\"reason\":\"droppableUntil must be the following format\\\"(\\\\d{4}-\\\\d{2}-\\\\d{2}T\\\\d{2}:\\\\d{2}:\\\\d{2}.\\\\d{3}Z\\\",e.g.\\\"2020-12-31T23:59:59.000Z\\\"\"}]}"),
        ("deny adding invalid Exam [garbage UserId]", s"C.1:Garbage:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", "Garbage", testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"exam.lecturerEnrollmentId\",\"reason\":\"The user trying to add an exam is not registered in the system.\"}]}"),
        ("deny adding invalid Exam [garbage ModuleId]", s"C.1:$testUser2:Garbage:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, "Garbage", "Written Exam", 6, "2021-03-14T12:00:0.000Z0", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          ""),
        ("deny adding invalid Exam [garbage ExamType]", s"C.1:$testUser2:$testModule1:Garbage:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Garbage", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          ""),
        ("deny adding invalid Exam [date lastYear]", s"C.1:$testUser2:$testModule1:Written Exam:6:2020-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2020-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "Known Error, replace with correct one"),
        ("deny adding invalid Exam [admittableDate lastYear]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2020-03-10T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "Known Error, replace with correct one"),
        ("deny adding invalid Exam [droppableDate lastYear]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2020-03-12T12:00:00.000Z",
          "Known Error, replace with correct one"),
        ("deny adding invalid Exam [admit > date > drop]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-15T12:00:00.000Z", "2021-03-12T12:00:00.000Z",
          "Known Error, replace with correct one"),
        ("deny adding invalid Exam [admit > drop > date]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-16T12:00:00.000Z", "2021-03-15T12:00:00.000Z",
          "Known Error, replace with correct one"),
        ("deny adding invalid Exam [drop > date > admit]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-15T12:00:00.000Z",
          "Known Error, replace with correct one"),
        ("deny adding invalid Exam [drop > admit > date]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-15T12:00:00.000Z", "2021-03-16T12:00:00.000Z",
          "Known Error, replace with correct one"),
        ("deny adding invalid Exam [date > admit > drop]", s"C.1:$testUser2:$testModule1:Written Exam:6:2021-03-14T12:00:00.000Z", "C.1", testUser2, testModule1, "Written Exam", 6, "2021-03-14T12:00:00.000Z", "2021-03-10T12:00:00.000Z", "2021-03-08T12:00:00.000Z",
          "Known Error, replace with correct one")
      )
      for ((statement: String, examId: String, courseId: String, lecturerId: String, moduleId: String, examType: String, ects: Int, date: String, admitUntil: String, dropUntil: String, expectedError) <- testDataDeny) {
        s"$statement" in {
          Logger.info(s"Begin test: $statement")

          // prepare data
          val testExam = TestDataExam.customizableExam(examId, courseId, lecturerId, moduleId, examType, date, ects, admitUntil, dropUntil)

          // forge approval (lecturer)
          val result = intercept[TransactionExceptionTrait](chaincodeConnection.addExam(testExam))

          // test
          TestHelper.testTransactionResult(result, "addExam", expectedError)
        }
      }
      "deny adding valid Exam with no approvals" in {
        Logger.info(s"Begin test: deny adding with no approvals")

        // prepare data
        val testExam = TestDataExam.validFutureExam("C.1", testUser1, testModule1, "Written Exam", 6)

        // implicit approval but not enough
        val result = intercept[TransactionExceptionTrait](chaincodeConnection.addExam(testExam))

        // test
        TestHelper.testTransactionResult(result, "addExam", "{\"type\":\"HLInsufficientApprovals\",\"title\":\"The approvals present on the ledger do not suffice to execute this transaction\"}")
      }
    }

    "invoked with getExams correctly " should {
      val testData: Seq[(String, Seq[String], Seq[String], Seq[String], Seq[String], Seq[String], String, String)] = Seq(
        ("allow for getting all Exams", Seq(), Seq(), Seq(), Seq(), Seq(), "", ""),
        ("allow for getting all Exams via courseId", Seq(), Seq("C.1"), Seq(), Seq(), Seq(), "", ""),
        ("allow for getting all Exams via lecturerId", Seq(), Seq(), Seq(testUser1), Seq(), Seq(), "", ""),
        ("allow for getting all Exams via moduleId", Seq(), Seq(), Seq(), Seq(testModule1), Seq(), "", ""),
        ("allow for getting all Exams via admittableAt", Seq(), Seq(), Seq(), Seq(testModule1), Seq(), TestHelperStrings.getCurrentDate, ""),
        ("allow for getting all Exams via droppableAt", Seq(), Seq(), Seq(), Seq(testModule1), Seq(), "", TestHelperStrings.getCurrentDate),
        ("allow for getting all Written Exams via examType", Seq(), Seq(), Seq(), Seq(), Seq("Written Exam"), "", ""),
      // TODO: enable once Oral Exams are supported ("allow for getting all Oral Exams via examType", Seq(), Seq(), Seq(), Seq(), Seq("Oral Exam"), "", "")
      )
      for (
        (statement: String, examIds: Seq[String], courseIds: Seq[String], lecturerIds: Seq[String], moduleIds: Seq[String],
          types: Seq[String], admittableAt: String, droppableAt: String) <- testData
      ) {
        s"$statement [$examIds, $courseIds, $lecturerIds, $moduleIds, $types, $admittableAt, $droppableAt]" in {
          Logger.info(s"Begin test: $statement with [$examIds, $courseIds, $lecturerIds, $moduleIds, $types, $admittableAt, $droppableAt]")

          // should not throw exception
          val testResult: String = chaincodeConnection.getExams(examIds.toList, courseIds.toList, lecturerIds.toList, moduleIds.toList,
            types.toList, admittableAt, droppableAt)
          val allExams: String = chaincodeConnection.getExams(Seq(), Seq(), Seq(), Seq(), Seq(), "", "")

          TestHelperStrings.compareJson(allExams, testResult)
        }
      }
    }
  }
}