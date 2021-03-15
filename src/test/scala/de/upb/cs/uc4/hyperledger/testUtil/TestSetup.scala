package de.upb.cs.uc4.hyperledger.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionExamTrait, ConnectionExaminationRegulationTrait, ConnectionGroupTrait, ConnectionMatriculationTrait, ConnectionOperationTrait }
import de.upb.cs.uc4.hyperledger.testData.{ TestDataExaminationRegulation, TestDataGroup, TestDataMatriculation }
import de.upb.cs.uc4.hyperledger.testData.TestDataMatriculation.testModule
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, StringHelper }

object TestSetup {
  def setupExaminationRegulations(erConnection: ConnectionExaminationRegulationTrait, testNamePrefix: String): Unit = {
    // prepare data
    val modules1 = Seq(TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_1"), TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_2"))
    val modules2 = Seq(TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_3"), TestDataExaminationRegulation.getModule(testNamePrefix + "_Module_4"))
    val openER = TestDataExaminationRegulation.validExaminationRegulation(testNamePrefix + "_ER_Open1", modules1, isOpen = true)
    val closedER = TestDataExaminationRegulation.validExaminationRegulation(testNamePrefix + "_ER_Closed1", modules2, isOpen = false)

    // store on chain
    trySetupConnections("setupExaminationRegulations", () => {
      erConnection.addExaminationRegulation(openER)
      erConnection.addExaminationRegulation(closedER)
    })

    erConnection.close()
  }

  def establishAdminAndSystemGroup(connection: ConnectionGroupTrait, userName: String): Unit =
    establishGroups(connection, userName, TestDataGroup.adminGroupName, TestDataGroup.systemGroupName)
  def establishGroups(connection: ConnectionGroupTrait, userName: String, groups: String*): Unit = {
    // store on chain
    trySetupConnections("establishGroups", () => {
      groups.foreach(group => connection.addUserToGroup(userName, group))
    })

    // close
    connection.close()
  }

  def establishExamRegs(connection: ConnectionExaminationRegulationTrait, getOperationConnection: String => ConnectionOperationTrait, approvalUsers: Seq[String], examRegs: Seq[String]): Unit = {
    // store on chain
    examRegs.foreach(examReg => {
      trySetupConnections("establishExams", () => {
        approvalUsers.foreach(user => {
          getOperationConnection(user).initiateOperation(user, "UC4.ExaminationRegulation", "addExaminationRegulation", examReg)
        })
        connection.addExaminationRegulation(examReg)
      })
    })

    // close
    connection.close()
  }

  def establishExams(connection: ConnectionExamTrait, getOperationConnection: String => ConnectionOperationTrait, approvalUsers: Seq[String], exams: Seq[String]): Unit = {
    // store on chain
    exams.foreach(exam => {
      trySetupConnections("establishExams", () => {
        approvalUsers.foreach(user => {
          getOperationConnection(user).initiateOperation(user, "UC4.Exam", "addExam", exam)
        })
        connection.addExam(exam)
      })
    })

    // close
    connection.close()
  }

  def establishMatriculation(connection: ConnectionMatriculationTrait, getOperationConnection: String => ConnectionOperationTrait, approvalUsers: Seq[String], mat: String): Unit = {
    // store on chain
    trySetupConnections("establishMatriculation", () => {
      approvalUsers.foreach(user => {
        getOperationConnection(user).initiateOperation(user, "UC4.MatriculationData", "addMatriculationData", mat)
      })
      connection.addMatriculationData(mat)
    })

    // close
    connection.close()
  }

  def establishAdmissions(connection: ConnectionAdmissionTrait, getOperationConnection: String => ConnectionOperationTrait, approvalUsers: Seq[String], admissions: Seq[String]): Unit = {
    // store on chain
    admissions.foreach(admission => {
      trySetupConnections("establishCourseAdmissions", () => {
        approvalUsers.foreach(user => {
          getOperationConnection(user).initiateOperation(user, "UC4.Admission", "addAdmission", admission)
        })
        connection.addAdmission(admission)
      })
    })

    // close
    connection.close()
  }

  def establishExistingMatriculation(matConnection: ConnectionMatriculationTrait, operationConnection: ConnectionOperationTrait, matriculationTarget: String, matriculationData: String): Unit = {
    // approvals
    operationConnection.initiateOperation(matriculationTarget, "UC4.MatriculationData", "addMatriculationData", matriculationData)

    // store on chain
    trySetupConnections("establishExistingMatriculation", () => {
      matConnection.addMatriculationData(matriculationData)
    })

    matConnection.close()
  }

  def establishExaminationRegulations(connection: ConnectionExaminationRegulationTrait): Unit = {
    // prepare data
    val names = Seq("Computer Science", "Mathematics", "Media Sciences")

    // store on chain
    for (name: String <- names) {
      trySetupConnections("establishExaminationRegulation", () => {
        this.establishExaminationRegulation(connection, name)
      })
    }

    connection.close()
  }

  private def establishExaminationRegulation(connection: ConnectionExaminationRegulationTrait, name: String): Unit = {
    val existingValue = connection.getExaminationRegulations(StringHelper.parameterArrayToJson(Seq("\"" + name + "\"")))
    if (existingValue == "[]") {
      val examinationRegulation = TestDataExaminationRegulation.validExaminationRegulation(name, Seq(testModule("MatriculationTestModule.1"), testModule("MatriculationTestModule.2")), isOpen = true)
      connection.addExaminationRegulation(examinationRegulation)
    }
  }

  def trySetupConnections(actionName: String, functions: (() => Any)*): Unit = {
    functions.foreach(function => {
      try {
        function.apply()
      }
      catch {
        case e: Throwable => Logger.err(s"Error during $actionName: ", e)
      }
    })
  }
}
