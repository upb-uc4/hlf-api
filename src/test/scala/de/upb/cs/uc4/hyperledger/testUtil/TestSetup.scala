package de.upb.cs.uc4.hyperledger.tests.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionExaminationRegulationTrait, ConnectionGroupTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.tests.testUtil.TestDataMatriculation.testModule

object TestSetup {
  def setupExaminationRegulations(erConnection: ConnectionExaminationRegulationTrait): Unit = {
    // prepare data
    val modules1 = Seq(TestDataExaminationRegulation.getModule("AdmissionModule_1"), TestDataExaminationRegulation.getModule("AdmissionModule_2"))
    val modules2 = Seq(TestDataExaminationRegulation.getModule("AdmissionModule_3"), TestDataExaminationRegulation.getModule("AdmissionModule_4"))
    val openER = TestDataExaminationRegulation.validExaminationRegulation("AdmissionER_Open1", modules1, state = true)
    val closedER = TestDataExaminationRegulation.validExaminationRegulation("AdmissionER_Closed1", modules2, state = false)

    // store on chain
    TestHelper.trySetupConnections("setupExaminationRegulations", () => {
      erConnection.addExaminationRegulation(openER)
      erConnection.addExaminationRegulation(closedER)
    })

    erConnection.close()
  }

  def setupMatriculations(matConnection: ConnectionMatriculationTrait): Unit = {
    // prepare data
    val mat1 = TestDataMatriculation.validMatriculationDataCustom("AdmissionStudent_1", "AdmissionER_Open1")
    val mat2 = TestDataMatriculation.validMatriculationDataCustom("AdmissionStudent_2", "AdmissionER_Closed1")

    // store on chain
    TestHelper.trySetupConnections("setupMatriculations", () => {
      matConnection.addMatriculationData(mat1)
      matConnection.addMatriculationData(mat2)
    })

    matConnection.close()
  }

  def establishAdminGroup(connection: ConnectionGroupTrait, userName: String): Unit = {
    // store on chain
    TestHelper.trySetupConnections("establishAdminGroup", () => {
      connection.addUserToGroup(userName, "admin")
    })

    connection.close()
  }

  def establishExistingMatriculation(matConnection: ConnectionMatriculationTrait, existingMatriculationId: String): Unit = {
    // prepare data
    val mat1 = TestDataMatriculation.validMatriculationData1(existingMatriculationId)

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
