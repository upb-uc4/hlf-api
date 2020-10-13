lazy val hyperledger_api = (project in file("."))
  .settings(
    Commons.projectSettings("hlf_api"),
    Commons.gpgSettings(),
    Commons.commonSettings(),
    description := "Scala API to access our UC4 contracts/chaincodes.",
    libraryDependencies ++= Dependencies.scalaTestDependencies,
    libraryDependencies ++= Dependencies.hyperledgerDependencies,
  )
  .enablePlugins(GitVersioning)