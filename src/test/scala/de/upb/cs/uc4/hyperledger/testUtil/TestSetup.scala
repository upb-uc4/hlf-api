package de.upb.cs.uc4.hyperledger.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionExamTrait, ConnectionExaminationRegulationTrait, ConnectionGroupTrait, ConnectionMatriculationTrait, ConnectionOperationTrait }
import de.upb.cs.uc4.hyperledger.testUtil.TestDataMatriculation.testModule

object TestSetup {
  def setupExaminationRegulations(erConnection: ConnectionExaminationRegulationTrait, testNamePrefix: String): Unit = {
    // prepare data
    val modules1 = Seq(TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_1"), TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_2"))
    val modules2 = Seq(TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_3"), TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_4"))
    val openER = TestDataExaminationRegulation.validExaminationRegulation(testNamePrefix + "_ER_Open1", modules1, state = true)
    val closedER = TestDataExaminationRegulation.validExaminationRegulation(testNamePrefix + "_ER_Closed1", modules2, state = false)

    // store on chain
    TestHelper.trySetupConnections("setupExaminationRegulations", () => {
      erConnection.addExaminationRegulation(openER)
      erConnection.addExaminationRegulation(closedER)
    })

    erConnection.close()
  }

  def establishAdminAndSystemGroup(connection: ConnectionGroupTrait, userName: String): Unit =
    establishGroups(connection, userName, TestDataGroup.adminGroupName, TestDataGroup.systemGroupName)
  def establishGroups(connection: ConnectionGroupTrait, userName: String, groups: String*): Unit = {
    // store on chain
    TestHelper.trySetupConnections("establishGroups", () => {
      groups.foreach(group => connection.addUserToGroup(userName, group))
    })

    // close
    connection.close()
  }

  def establishExams(connection: ConnectionExamTrait, f: (String) => ConnectionOperationTrait, approvalUsers: Seq[String], exams: Seq[String]): Unit = {
    // store on chain
    TestHelper.trySetupConnections("establishExams", () => {
      exams.foreach(exam => {
        approvalUsers.foreach(user => {
          f(user).initiateOperation(user, "UC4.Exam", "addExam", exam)
          connection.addExam(exam)
        })
      })
    })

    // close
    connection.close()
  }

  def establishExistingMatriculation(matConnection: ConnectionMatriculationTrait, operationConnection: ConnectionOperationTrait, existingMatriculationId: String): Unit = {
    // prepare data
    val mat1 = TestDataMatriculation.validMatriculationData1(existingMatriculationId)

    // approvals
    operationConnection.initiateOperation(existingMatriculationId, "UC4.MatriculationData", "addMatriculationData", mat1)

    // store on chain
    TestHelper.trySetupConnections("establishExistingMatriculation", () => {
      matConnection.addMatriculationData(mat1)
    })

    matConnection.close()
  }

  def establishExaminationRegulations(connection: ConnectionExaminationRegulationTrait): Unit = {
    // prepare data
    val names = Seq("Computer Science", "Mathematics", "Media Sciences")

    // store on chain
    for (name: String <- names) {
      TestHelper.trySetupConnections("establishExaminationRegulation", () => {
        this.establishExaminationRegulation(connection, name)
      })
    }

    connection.close()
  }

  private def establishExaminationRegulation(connection: ConnectionExaminationRegulationTrait, name: String): Unit = {
    val existingValue = connection.getExaminationRegulations(TestHelperStrings.getJsonList(Seq("\"" + name + "\"")))
    if (existingValue == "[]") {
      val examinationRegulation = TestDataExaminationRegulation.validExaminationRegulation(name, Seq(testModule("MatriculationTestModule.1"), testModule("MatriculationTestModule.2")), state = true)
      connection.addExaminationRegulation(examinationRegulation)
    }
  }
}
