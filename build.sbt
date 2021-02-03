import sbt._

description := "Scala API to access our UC4 contracts/chaincodes."

// settings
Commons.projectInfo()
Commons.projectSettings("hlf-api")

// dependencies
libraryDependencies ++= Dependencies.scalaTestDependencies
libraryDependencies ++= Dependencies.hyperledgerDependencies
libraryDependencies ++= Dependencies.sl4j


// plugins
enablePlugins(GitVersioning, BuildInfoPlugin)

// make tests sequential
parallelExecution in Test := false