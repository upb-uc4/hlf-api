package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{Path, Paths}

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait TestBaseTrait extends AnyWordSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {
  val contractNameCourse: String = "UC4.course"
  val contractNameMatriculation: String = "UC4.MatriculationData"
  val walletPath: Path = Paths.get(getClass.getResource("/wallet/").toURI)

  val networkDescriptionPath: Path
  val minikubeIP: String
  val caURL: String
  val tlsCert: Path
  val username: String
  val password: String
  val organisationId: String
  val channel: String
  val chaincode: String
}
