import sbt.Keys._
import sbt.{ TestFrameworks, Tests }

object Commons {
  def commonSettings(project: String) = Seq(
    organization := "de.upb.cs.uc4",
    version := "v0.6.2",
    scalaVersion := "2.13.0",
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test_reports/" + project)
  )
}
