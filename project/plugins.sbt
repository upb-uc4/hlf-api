// Enables code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
// Scala auto formatting tool
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
// Publish to sonatype - maven
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")