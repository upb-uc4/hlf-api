package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil._
import de.upb.cs.uc4.hyperledger.testData.{ TestDataAdmission, TestDataExam, TestDataGroup }
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, StringHelper }

class AdmissionTests_Exam extends TestBase {

  val chaincodeConnection: ConnectionAdmissionTrait = initializeAdmission()

  val testNamePrefix = "ExamTest"

  val testModule1: String = testNamePrefix + "_Module_1"
  val testModule2: String = testNamePrefix + "_Module_2"
  val testModule3: String = testNamePrefix + "_Module_3"
  val testModule4: String = testNamePrefix + "_Module_4"

  val lecturer1 = "lecturer_1"
  val lecturer2 = "lecturer_2"

  val student1 = "student_1"
  val student2 = "student_2"

  val testExam1: String = TestDataExam.validFutureExam("ExamAdmissionCourse1", lecturer1, testModule1, "Written Exam", 6)
  val testExam2: String = TestDataExam.validFutureExam("ExamAdmissionCourse2", lecturer1, testModule2, "Written Exam", 5)
  val testExam3: String = TestDataExam.validFutureExam("ExamAdmissionCourse3", lecturer1, testModule3, "Written Exam", 6)
  val testExam4: String = TestDataExam.validFutureExam("ExamAdmissionCourse4", lecturer2, testModule3, "Written Exam", 6)
  val testExam5: String = TestDataExam.validFutureExam("ExamAdmissionCourse5", lecturer2, testModule4, "Written Exam", 6)
  val testExam6: String = TestDataExam.validFutureExam("ExamAdmissionCourse6", lecturer2, testModule1, "Written Exam", 5)

  val examId1: String = TestDataExam.calculateExamId(testExam1)
  val examId2: String = TestDataExam.calculateExamId(testExam2)
  val examId3: String = TestDataExam.calculateExamId(testExam3)
  val examId4: String = TestDataExam.calculateExamId(testExam4)
  val examId5: String = TestDataExam.calculateExamId(testExam5)
  val examId6: String = TestDataExam.calculateExamId(testExam6)

  override def beforeAll(): Unit = {
    super.beforeAll()
    // TODO: RESET LEDGER
    TestSetup.setupExaminationRegulations(initializeExaminationRegulation(), testNamePrefix)
    prepareUser(lecturer1)
    prepareUser(lecturer2)
    prepareUser(student1)
    prepareUser(student2)
    TestSetup.establishGroups(initializeGroup(), lecturer1, TestDataGroup.lecturerGroupName)
    TestSetup.establishGroups(initializeGroup(), lecturer2, TestDataGroup.lecturerGroupName)
    TestSetup.establishExams(initializeExam(), initializeOperation, Seq(lecturer1, lecturer2), Seq(testExam1, testExam2, testExam3, testExam4, testExam5, testExam6))
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    // TODO: RESET LEDGER
    super.afterAll()
  }

  "The ScalaAPI for ExamAdmissions" when {
    "invoked with addAdmission correctly " should {
      val testDataAllow: Seq[(String, String, String)] = Seq(
        ("allow for adding new valid ExamAdmission (Approvals forged)", student1, examId1),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student1, examId2),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student1, examId3),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student1, examId4),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student1, examId5),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student1, examId6),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student2, examId2),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student2, examId3),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student2, examId4),
        ("allow for adding new valid ExamAdmission (Approvals forged)", student2, examId5),
      )
      for ((statement: String, enrollmentId: String, examId: String) <- testDataAllow) {
        s"$statement [$enrollmentId, $examId]" in {
          Logger.info(s"Begin test: $statement [$enrollmentId, $examId]")
          val testAdmission = TestDataAdmission.validExamAdmission(enrollmentId, examId)

          // forge approval (lecturer)
          initializeOperation(enrollmentId).initiateOperation(enrollmentId, "UC4.Admission", "addAdmission", testAdmission)

          // implicit approval (system, admin)
          val testResult = chaincodeConnection.addAdmission(testAdmission)

          // test
          val expectedResult = testAdmission
          TestHelperStrings.compareJson(expectedResult, testResult)
        }
      }
    }

    "invoked with addAdmission in exam context incorrectly " should {
      val testDataDeny: Seq[(String, String, String, String, String, String)] = Seq(
        // timestamp should be generated ("deny adding invalid Exam Admission [empty timestamp]", student1, examId1, "", "Exam", "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.date\",\"reason\":\"The given parameter must not be empty\"}]}"),
        // timestamp should be generated ("deny adding invalid Exam Admission [garbage timestamp]", student1, examId1, "GARBAGE", "Exam", "Some error."),
        ("deny adding invalid Exam Admission [empty enrollmentId]", "", examId1, "2021-03-12T12:00:00", "Exam",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.enrollmentId\",\"reason\":\"The given parameter must not be empty\"}]}"),
        ("deny adding invalid Exam Admission [empty examId]", student1, "", "2021-03-12T12:00:00", "Exam",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.examId\",\"reason\":\"The given parameter must not be empty\"}]}"),
        ("deny adding invalid Exam Admission [empty admissionType]", student1, examId1, "2021-03-12T12:00:00", "",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission\",\"reason\":\"The given parameter can not be parsed from json\"}]}"),
        ("deny adding invalid Exam Admission [garbage enrollmentId]", "Garbage", examId1, "2021-03-12T12:00:00", "Exam",
          "Some error."),
        ("deny adding invalid Exam Admission [garbage examId]", student1, "Garbage", "2021-03-12T12:00:00", "Exam",
          "Some error."),
        ("deny adding invalid Exam Admission [garbage admissionType]", student1, examId1, "2021-03-12T12:00:00", "Garbage",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission\",\"reason\":\"The given parameter can not be parsed from json\"}]}"),
        ("deny adding invalid Exam Admission [wrong admissionType]", student1, examId1, "2021-03-12T12:00:00", "Course",
          "[{\"type\":\"HLUnprocessableEntity\",\"title\":\"Thefollowingparametersdonotconformtothespecifiedformat\",\"invalidParams\":[{\"name\":\"admission.courseId\",\"reason\":\"Thegivenparametermustnotbeempty\"},{\"name\":\"admission.moduleId\",\"reason\":\"Thegivenparametermustnotbeempty\"},{\"name\":\"admission.enrollmentId\",\"reason\":\"ThestudentisnotmatriculatedinanyexaminationRegulationcontainingthemoduleheistryingtoenrollin\"},{\"name\":\"admission.moduleId\",\"reason\":\"ThestudentisnotmatriculatedinanyexaminationRegulationcontainingthemoduleheistryingtoenrollin\"}]}")
      )
      for ((statement: String, enrollmentId: String, examId: String, timestamp: String, admissionType: String, expectedError: String) <- testDataDeny) {
        s"$statement" in {
          Logger.info(s"Begin test: $statement")
          val testAdmission = TestDataAdmission.customizableExamAdmission(enrollmentId, examId, timestamp, admissionType)

          // forge approval (lecturer)
          val result = intercept[TransactionExceptionTrait](initializeOperation(username).initiateOperation(enrollmentId, "UC4.Admission", "addAdmission", testAdmission))

          // test
          TestHelper.testTransactionResult(result, "initiateOperation", expectedError)
        }
      }
    }

    "invoked with getExamAdmissions correctly " should {
      val testData: Seq[(String, Seq[String], String, Seq[String], Int)] = Seq(
        ("allow for getting all ExamAdmissions", Seq(), "", Seq(), 10),
        ("allow for getting all ExamAdmissions for student", Seq(), student1, Seq(), 6),
        ("allow for getting all ExamAdmissions for examIds", Seq(), "", Seq(examId1, examId2, examId3), 5),
        ("allow for getting all ExamAdmissions for multiple filters", Seq(), student2, Seq(examId1, examId2, examId4, examId5, examId6), 3)
      )
      for (
        (statement: String, admissionIds: Seq[String], enrollmentId: String, examIds: Seq[String], expectedCount: Int) <- testData
      ) {
        s"$statement [$admissionIds, $enrollmentId, $examIds, $expectedCount]" in {
          Logger.info(s"Begin test: $statement [$admissionIds, $enrollmentId, $examIds, $expectedCount]")

          // should not throw exception
          val testResult: String = chaincodeConnection.getExamAdmissions(admissionIds.toList, enrollmentId, examIds.toList)
          Logger.debug("ADMISSION GET: " + testResult)
          val examAdmissions: Array[Object] = StringHelper.objectArrayFromJson(testResult)

          examAdmissions.length should be(expectedCount)
        }
      }
    }
  }
}