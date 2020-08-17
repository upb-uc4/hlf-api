package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{Path, Paths}

import de.upb.cs.uc4.hyperledger.connections.cases.{ConnectionCourses, ConnectionMatriculation}
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCourseTrait, ConnectionMatriculationTrait}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestBaseDevNetwork extends TestBaseGeneral {

  val networkDescriptionPath: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val username: String = "cli"
  val channel: String = "myc"
  val chaincode: String = "mycc"

  def initializeCourses(): ConnectionCourseTrait = new ConnectionCourses(username, channel, chaincode, walletPath, networkDescriptionPath)
  def initializeMatriculation(): ConnectionMatriculationTrait = new ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)


}
