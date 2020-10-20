package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase

class VersionAccessTests extends TestBase {

  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  var regexVersionString: String = """v((\d)+\.){2}(\d)+"""

  "The ScalaAPI for Connections" when {
    "asked for chaincode version " should {
      "provide a valid endpoint [Certificate] " in {
        certificateConnection = initializeCertificate()
        val version: String = certificateConnection.getVersion
        version should fullyMatch regex regexVersionString
        certificateConnection.close()
      }
      "provide a valid endpoint [Certificate] " in {
        matriculationConnection = initializeMatriculation()
        val version: String = matriculationConnection.getVersion
        version should fullyMatch regex regexVersionString
        matriculationConnection.close()
      }
    }
  }
}