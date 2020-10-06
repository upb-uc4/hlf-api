import sbt.Keys._
import sbt.{Developer, ScmInfo, TestFrameworks, Tests, url}

object Commons {
  def commonSettings(project: String) = Seq(
    organization := "de.upb.cs.uc4",
    organizationName := "uc4",
    homepage := Some(url("https://uc4.cs.upb.de/")),
    scmInfo := Some(ScmInfo(url("https://github.com/upb-uc4/hlf-api"), "git@github.com:upb-uc4/hlf-api.git")),
    version := "v0.9.1",
    developers := List(Developer("UC4", "UC4", "UC4_official@web.de", url("https://github.com/upb-uc4"))),
    licenses := List("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    publishMavenStyle := true,
    crossPaths := false,
    scalaVersion := "2.13.0",
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test_reports/" + project)
  )
}
