import sbt._

lazy val hlf-api = (project in file("."))
  .settings(
    Commons.projectInfo(),
    Commons.projectSettings("hlf-api"),
    Commons.gpgSettings(),
    description := "Scala API to access our UC4 contracts/chaincodes.",
    libraryDependencies ++= Dependencies.scalaTestDependencies,
    libraryDependencies ++= Dependencies.hyperledgerDependencies,
  )
  .enablePlugins(
    GitVersioning,
    BuildInfoPlugin)