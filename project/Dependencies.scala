import sbt._

object Dependencies {
  // libraries
  private val hyperledgerSDK = "org.hyperledger.fabric-sdk-java" % "fabric-sdk-java" % "2.2.5"
  private val hyperledgerGateway = "org.hyperledger.fabric" % "fabric-gateway-java" % "2.2.1"
  private val scalaTest = "org.scalatest" %% "scalatest" % "3.2.3" % Test
  private val flexmark = "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test

  // dependency groups
  val hyperledgerDependencies = Seq(hyperledgerSDK, hyperledgerGateway)
  val scalaTestDependencies = Seq(scalaTest, flexmark)
}