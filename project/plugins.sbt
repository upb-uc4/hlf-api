// Enables code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
// Scala auto formatting tool
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
// buildinfo
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
// ---------------------------
// release plugin
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.3")
// public key generation
// addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
/* below plugins are already brought in by sbt-ci-release
// Publish to sonatype - maven
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
// git versioning
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
// dynver
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
*/
// ---------------------------