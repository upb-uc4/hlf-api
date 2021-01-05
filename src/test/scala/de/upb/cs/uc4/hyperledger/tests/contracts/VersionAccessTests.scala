package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionExaminationRegulationTrait, ConnectionMatriculationTrait, ConnectionOperationsTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase

class VersionAccessTests extends TestBase {

  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _
  var approvalConnection: ConnectionOperationsTrait = _
  var ERConnection: ConnectionExaminationRegulationTrait = _

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    approvalConnection.close()
    ERConnection.close()
    super.afterAll()
  }

  val regexVersionString: String = """((\d)+\.){2}(\d)+"""

  "The ScalaAPI for Connections" when {
    "asked for chaincode version " should {
      "provide a valid endpoint [Certificate] " in {
        certificateConnection = initializeCertificate()
        val version: String = certificateConnection.getChaincodeVersion
        version should fullyMatch regex regexVersionString
        certificateConnection.close()
      }
      "provide a valid endpoint [Matriculation] " in {
        matriculationConnection = initializeMatriculation()
        val version: String = matriculationConnection.getChaincodeVersion
        version should fullyMatch regex regexVersionString
        matriculationConnection.close()
      }
      "provide a valid endpoint [Approval] " in {
        approvalConnection = initializeOperation()
        val version: String = approvalConnection.getChaincodeVersion
        version should fullyMatch regex regexVersionString
        approvalConnection.close()
      }
      "provide a valid endpoint [ExaminationRegulation] " in {
        ERConnection = initializeExaminationRegulation()
        val version: String = ERConnection.getChaincodeVersion
        version should fullyMatch regex regexVersionString
        ERConnection.close()
      }
    }
  }
}