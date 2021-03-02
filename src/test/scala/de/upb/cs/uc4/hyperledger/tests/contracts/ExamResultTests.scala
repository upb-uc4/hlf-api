package de.upb.cs.uc4.hyperledger.tests.contracts

import java.util.{ Calendar, Date }

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExamResultTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData._
import de.upb.cs.uc4.hyperledger.testUtil._
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class ExamResultTests extends TestBase {

  val chaincodeConnection: ConnectionExamResultTrait = initializeExamResult()

  val testNamePrefix = "ExamResultTest"

  val testModule1: String = testNamePrefix + "_Module_1"
  val testModule2: String = testNamePrefix + "_Module_2"
  def testModuleItem(testModuleId: String): String = TestDataExaminationRegulation.getModule(testModuleId, testModuleId + "ShortName")

  val testCourse1: String = testNamePrefix + "_Course1"
  val testCourse2: String = testNamePrefix + "_Course2"
  val testCourse3: String = testNamePrefix + "_Course3"
  val testCourse4: String = testNamePrefix + "_Course4"

  val testExamReg1: String = testNamePrefix + "_ER_Open1"

  val testExamRegItem1: String = TestDataExaminationRegulation.validExaminationRegulation(
    testExamReg1, Seq(testModuleItem(testModule1), testModuleItem(testModule2)), isOpen = true
  )

  val lecturer1: String = testNamePrefix + "_Lecturer_1"
  val lecturer2: String = testNamePrefix + "_Lecturer_2"

  val student1: String = testNamePrefix + "_Student_1"
  val student2: String = testNamePrefix + "_Student_2"
  val student3: String = testNamePrefix + "_Student_3"
  val student4: String = testNamePrefix + "_Student_4"

  val current: Calendar = Calendar.getInstance()
  current.add(Calendar.MINUTE, 10)
  val examTime: Date = current.getTime
  val testExam1: String = TestDataExam.validFutureExam(testCourse1, lecturer1, testModule1, "Written Exam", 6)
  val testExam2: String = TestDataExam.validFutureExam(testCourse2, lecturer1, testModule1, "Written Exam", 5)
  val testExam3: String = TestDataExam.validFutureExam(testCourse3, lecturer2, testModule2, "Written Exam", 4)
  val testExam4: String = TestDataExam.validFutureExam(testCourse4, lecturer2, testModule2, "Written Exam", 3)

  def testMat(studentId: String): String = TestDataMatriculation.validMatriculationDataCustom_MultipleExamRegs(studentId, Seq(testExamReg1))

  def testCourseAdmission1(studentId: String): String = TestDataAdmission.validCourseAdmission(studentId, testCourse1, testModule1, "")
  def testCourseAdmission2(studentId: String): String = TestDataAdmission.validCourseAdmission(studentId, testCourse2, testModule1, "")
  def testCourseAdmission3(studentId: String): String = TestDataAdmission.validCourseAdmission(studentId, testCourse3, testModule2, "")
  def testCourseAdmission4(studentId: String): String = TestDataAdmission.validCourseAdmission(studentId, testCourse4, testModule2, "")

  val examId1: String = TestDataExam.calculateExamId(testExam1)
  val examId2: String = TestDataExam.calculateExamId(testExam2)
  val examId3: String = TestDataExam.calculateExamId(testExam3)
  val examId4: String = TestDataExam.calculateExamId(testExam4)

  def testExamAdmission(studentId: String, examId: String): String = TestDataAdmission.validExamAdmission(studentId, examId)

  override def beforeAll(): Unit = {
    super.beforeAll()
    // TODO: RESET LEDGER
    prepareUsers(lecturer1, lecturer2, student1, student2, student3, student4)
    TestSetup.establishGroups(initializeGroup(), lecturer1, TestDataGroup.lecturerGroupName)
    TestSetup.establishGroups(initializeGroup(), lecturer2, TestDataGroup.lecturerGroupName)
    TestSetup.establishExamRegs(initializeExaminationRegulation(), initializeOperation, Seq(), Seq(testExamRegItem1))
    TestSetup.establishExams(initializeExam(), initializeOperation, Seq(lecturer1), Seq(testExam1, testExam2))
    TestSetup.establishExams(initializeExam(), initializeOperation, Seq(lecturer2), Seq(testExam3, testExam4))
    TestSetup.establishMatriculation(initializeMatriculation(), initializeOperation, Seq(student1), testMat(student1))
    TestSetup.establishMatriculation(initializeMatriculation(), initializeOperation, Seq(student2), testMat(student2))
    TestSetup.establishMatriculation(initializeMatriculation(), initializeOperation, Seq(student3), testMat(student3))
    TestSetup.establishMatriculation(initializeMatriculation(), initializeOperation, Seq(student4), testMat(student4))
    TestSetup.establishAdmissions(initializeAdmission(), initializeOperation, Seq(student1),
      Seq(
        testCourseAdmission1(student1), testCourseAdmission2(student1), testCourseAdmission3(student1), testCourseAdmission4(student1),
        testExamAdmission(student1, examId1), testExamAdmission(student1, examId2), testExamAdmission(student1, examId3)
      ))
    TestSetup.establishAdmissions(initializeAdmission(), initializeOperation, Seq(student2),
      Seq(
        testCourseAdmission1(student2), testCourseAdmission2(student2), testCourseAdmission3(student2), testCourseAdmission4(student2),
        testExamAdmission(student2, examId1), testExamAdmission(student2, examId2), testExamAdmission(student2, examId3)
      ))
    TestSetup.establishAdmissions(initializeAdmission(), initializeOperation, Seq(student3),
      Seq(
        testCourseAdmission1(student3), testCourseAdmission2(student3), testCourseAdmission3(student3), testCourseAdmission4(student3),
        testExamAdmission(student3, examId3), testExamAdmission(student3, examId4)
      ))
    TestSetup.establishAdmissions(initializeAdmission(), initializeOperation, Seq(student4),
      Seq(
        testCourseAdmission1(student4), testCourseAdmission2(student4), testCourseAdmission3(student4), testCourseAdmission4(student4),
        testExamAdmission(student4, examId3), testExamAdmission(student4, examId4)
      ))
    val currentTime = Calendar.getInstance().getTime
    val diffMillis = examTime.getTime - currentTime.getTime
    Logger.debug("DiffMillis: " + diffMillis)
    Thread.sleep(diffMillis)
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    // TODO: RESET LEDGER
    super.afterAll()
  }

  "The ScalaAPI for ExamResults" when {
    "invoked with addExamResult correctly " should {
      val testDataAllow: Seq[(String, String, Seq[(String, String, String)])] = Seq(
        ("allow for adding new valid ExamResults", lecturer2, Seq((student1, examId3, "1.0"), (student2, examId3, "2.0"), (student3, examId3, "2.0"), (student4, examId3, "4.0"))),
      )
      for ((statement: String, lecturer: String, entries: Seq[(String, String, String)]) <- testDataAllow) {
        s"$statement [$lecturer, $entries]" in {
          Logger.info(s"Begin test: $statement [$entries]")
          val jsonEntries: Seq[String] = entries.map(item => TestDataExamResult.customizableExamResultEntry(item._1, item._2, item._3))
          val testGrades = TestDataExamResult.customizableExamResult(jsonEntries)

          // forge approval (lecturer)
          initializeOperation(lecturer).initiateOperation(lecturer, "UC4.ExamResult", "addExamResult", testGrades)

          // implicit approval (system, admin)
          val testResult = chaincodeConnection.addExamResult(testGrades)

          // test
          TestHelper.compareExamResults(testGrades, testResult)
        }
      }
    }

    /*
    "invoked with addAdmission in exam context incorrectly " should {
      val testDataDeny: Seq[(String, String, String, String, String, String)] = Seq(
        // timestamp should be generated ("deny adding invalid Exam Admission [empty timestamp]", student1, examId1, "", "Exam", "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.date\",\"reason\":\"The given parameter must not be empty\"}]}"),
        // timestamp should be generated ("deny adding invalid Exam Admission [garbage timestamp]", student1, examId1, "GARBAGE", "Exam", "Some error."),
        ("deny adding invalid Exam Admission [empty enrollmentId]", "", examId1, "2021-03-12T12:00:00", "Exam",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.enrollmentId\",\"reason\":\"The given parameter must not be empty\"},{\"name\":\"admission.enrollmentId\",\"reason\":\"The student is not matriculated in any examinationRegulation\"},{\"name\":\"admission.enrollmentId\",\"reason\":\"ThestudentisnotmatriculatedinanyexaminationRegulationcontainingthemoduletheexamisreferencing.\"},{\"name\":\"admission.examId\",\"reason\":\"ThestudentisnotmatriculatedinanyexaminationRegulationcontainingthemoduletheexamisreferencing.\"},{\"name\":\"admission.enrollmentId\",\"reason\":\"Thestudentisnotadmittedinthecourseoftheexam.\"},{\"name\":\"admission.examId\",\"reason\":\"Thestudentisnotadmittedinthecourseoftheexam.\"}]}"),
        ("deny adding invalid Exam Admission [empty examId]", student1, "", "2021-03-12T12:00:00", "Exam",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.examId\",\"reason\":\"The given parameter must not be empty\"},{\"name\":\"admission.examId\",\"reason\":\"Theexamyouaretryingtoadmitfordoesnotexist.\"}]}"),
        ("deny adding invalid Exam Admission [empty admissionType]", student1, examId1, "2021-03-12T12:00:00", "",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.type\",\"reason\":\"The admission.type has/have to be one of {Course, Exam}\"}]}"),
        ("deny adding invalid Exam Admission [garbage enrollmentId]", "Garbage", examId1, "2021-03-12T12:00:00", "Exam",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.enrollmentId\",\"reason\":\"The student is not matriculated in any examinationRegulation\"},{\"name\":\"admission.enrollmentId\",\"reason\":\"ThestudentisnotmatriculatedinanyexaminationRegulationcontainingthemoduletheexamisreferencing.\"},{\"name\":\"admission.examId\",\"reason\":\"ThestudentisnotmatriculatedinanyexaminationRegulationcontainingthemoduletheexamisreferencing.\"},{\"name\":\"admission.enrollmentId\",\"reason\":\"Thestudentisnotadmittedinthecourseoftheexam.\"},{\"name\":\"admission.examId\",\"reason\":\"Thestudentisnotadmittedinthecourseoftheexam.\"}]}"),
        ("deny adding invalid Exam Admission [garbage examId]", student1, "Garbage", "2021-03-12T12:00:00", "Exam",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"Thefollowingparametersdonotconformtothespecifiedformat\",\"invalidParams\":[{\"name\":\"admission.examId\",\"reason\":\"The exam you are trying to admit for does not exist.\"}]}"),
        ("deny adding invalid Exam Admission [garbage admissionType]", student1, examId1, "2021-03-12T12:00:00", "Garbage",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.type\",\"reason\":\"The admission.type has/have to be one of {Course, Exam}\"}]}"),
        ("deny adding invalid Exam Admission [wrong admissionType]", student1, examId1, "2021-03-12T12:00:00", "Course",
          "{\"type\":\"HLUnprocessableEntity\",\"title\":\"The following parameters do not conform to the specified format\",\"invalidParams\":[{\"name\":\"admission.courseId\",\"reason\":\"The given parameter must not be empty\"},{\"name\":\"admission.moduleId\",\"reason\":\"Thegivenparametermustnotbeempty\"},{\"name\":\"admission.moduleId\",\"reason\":\"ThestudentisnotmatriculatedinanyexaminationRegulationcontainingthemoduleheistryingtoenrollin\"}]}")
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
        ("allow for getting all ExamAdmissions for examIds", Seq(), "", Seq(examId1, examId2, examId3), 5)
      )
      for (
        (statement: String, admissionIds: Seq[String], enrollmentId: String, examIds: Seq[String], expectedCount: Int) <- testData
      ) {
        s"$statement [$admissionIds, $enrollmentId, $examIds, $expectedCount]" in {
          Logger.info(s"Begin test: $statement [$admissionIds, $enrollmentId, $examIds, $expectedCount]")

          // should not throw exception
          val testResult: String = chaincodeConnection.getExamAdmissions(admissionIds.toList, enrollmentId, examIds.toList)
          val examAdmissions: Array[Object] = StringHelper.objectArrayFromJson(testResult)

          examAdmissions.length should be(expectedCount)
        }
      }
    }
    */
  }
}