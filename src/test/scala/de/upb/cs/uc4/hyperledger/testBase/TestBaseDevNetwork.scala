package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{ Path, Paths }

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionCourses, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCourseTrait, ConnectionMatriculationTrait }

protected class TestBaseDevNetwork extends TestBaseTrait {

  val networkDescriptionPath: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val username: String = "cli"
  val channel: String = "myc"
  val chaincode: String = "mycc"

  def initializeCourses(): ConnectionCourseTrait = new ConnectionCourses(username, channel, chaincode, walletPath, networkDescriptionPath)
  def initializeMatriculation(): ConnectionMatriculationTrait = ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)

  override val minikubeIP: String = null
  override val caURL: String = null
  override val tlsCert: Path = null
  override val password: String = null
  override val organisationId: String = null
}
