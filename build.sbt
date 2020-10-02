lazy val hyperledger_api = (project in file("."))
  .settings(
    Commons.commonSettings("hyperledger_api"),
    description := "Scala API to access our UC4 contracts/chaincodes.",
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")},
    libraryDependencies ++= Dependencies.scalaTestDependencies,
    libraryDependencies ++= Dependencies.hyperledgerDependencies,
  )