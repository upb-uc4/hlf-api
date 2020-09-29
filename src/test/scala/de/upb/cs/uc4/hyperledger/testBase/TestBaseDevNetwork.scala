package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{ Path, Paths }

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionCourses, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCourseTrait, ConnectionMatriculationTrait }

protected class TestBaseDevNetwork extends TestBaseTrait {
  val networkDescriptionPath: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val username: String = "cli"
  val channel: String = "myc"
  val chaincode: String = "mycc"
  // unused productionNetwork variables
  val minikubeIP: String = null
  val caURL: String = null
  val tlsCert: Path = null
  val password: String = null
  val organisationId: String = null
}
