package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{Path, Paths}

import de.upb.cs.uc4.hyperledger.connections.cases.{ConnectionCourses, ConnectionMatriculation}
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCourseTrait, ConnectionMatriculationTrait}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestBaseGeneral extends AnyWordSpec with Matchers with BeforeAndAfterEach{
  val contractNameCourse: String = "UC4.course"
  val contractNameMatriculation: String = "UC4.MatriculationData"
  val walletPath: Path = Paths.get(getClass.getResource("/wallet/").toURI)
}
