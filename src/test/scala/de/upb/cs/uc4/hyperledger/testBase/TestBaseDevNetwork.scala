package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{ Path, Paths }

import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionMatriculation
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait

protected class TestBaseDevNetwork extends TestBaseTrait {
  val networkDescriptionPath: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val username: String = "cli"
  val channel: String = "myc"
  val chaincode: String = "mycc"
  // unused productionNetwork variables
  val caURL: String = null
  val tlsCert: Path = null
  val password: String = null
  val organisationId: String = null
}
