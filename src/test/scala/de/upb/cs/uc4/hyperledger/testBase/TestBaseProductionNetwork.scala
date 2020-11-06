package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{ Path, Paths }

protected class TestBaseProductionNetwork extends TestBaseTrait {
  val networkDescriptionPath: Path = Paths.get(
    sys.env.getOrElse("UC4_CONNECTION_PROFILE", "./hlf-network/assets/connection_profile_kubernetes_local.yaml"))
  val channel: String = "mychannel"
  val chaincode: String = "uc4-cc"
  // productionNetwork variables
  private val NODE_IP: String = sys.env.getOrElse("UC4_KIND_NODE_IP", "172.17.0.2")
  val caURL = s"https://$NODE_IP:30907"
  val tlsCert: Path = Paths.get("/tmp/hyperledger/org1/msp/cacerts/org1-ca-cert.pem")
  val username: String = "test-admin"
  val password: String = "test-admin-pw"
  val organisationId: String = "org1MSP"
}
