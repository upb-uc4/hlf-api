import sbt._

object Dependencies {
  // libraries
  private val hyperledgerSDK = "org.hyperledger.fabric-sdk-java" % "fabric-sdk-java" % "2.2.5"
  private val hyperledgerGateway = "org.hyperledger.fabric" % "fabric-gateway-java" % "2.2.1"
  private val scalaTest = "org.scalatest" %% "scalatest" % "3.2.3" % Test
  private val flexmark = "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test
  private val sl4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  private val sl4jLog = "ch.qos.logback" % "logback-core" % "1.2.3"
  private val sl4jClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  // dependency groups
  val hyperledgerDependencies = Seq(hyperledgerSDK, hyperledgerGateway)
  val scalaTestDependencies = Seq(scalaTest, flexmark)
  val sl4j = Seq(sl4jApi, sl4jLog, sl4jClassic)
}