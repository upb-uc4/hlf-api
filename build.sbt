import sbt._

description := "Scala API to access our UC4 contracts/chaincodes."

// settings
Commons.projectInfo()
Commons.projectSettings("hlf-api")
Commons.gpgSettings()

// dependencies
libraryDependencies ++= Dependencies.scalaTestDependencies
libraryDependencies ++= Dependencies.hyperledgerDependencies
libraryDependencies += Dependencies.json

// plugins
enablePlugins(GitVersioning, BuildInfoPlugin)