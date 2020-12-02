package de.upb.cs.uc4.hyperledger.tests.sequentialSuites

import de.upb.cs.uc4.hyperledger.tests._
import org.scalatest.{ DoNotDiscover, Sequential }

@DoNotDiscover
class SequentialAdmissionTests extends Sequential(
  new AdmissionAccessTests
)
