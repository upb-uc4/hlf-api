import sbt.Keys._
import sbt.{ TestFrameworks, Tests }

object Commons {
  def commonSettings(project: String) = Seq(
    organization := "de.upb.cs.uc4",
    homepage := Some(url("https://uc4.cs.upb.de/")),
    scmInfo := Some(ScmInfo(url("https://github.com/upb-uc4/hlf-api"), "git@github.com:upb-uc4/hlf-api.git")),
    version := "v0.9.1",
    developers := List(
        Developer("UC4", "UC4", "UC4_official@web.de", url("https://github.com/upb-uc4"))
    ),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    publishMavenStyle := true,
    crossPaths := false,

    publishTo := Some(Opts.resolver.sonatypeStaging),

    // realease with sbt-release plugin
    import ReleaseTransformations._
    releaseCrossBuild := true,
    //releaseTagName := s"version-${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
    releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        releaseStepCommandAndRemaining("+publishSigned"),
        setNextVersion,
        commitNextVersion,
        releaseStepCommand("sonatypeRelease"),
        pushChanges
    ),

    scalaVersion := "2.13.0",
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test_reports/" + project)
  )
}
