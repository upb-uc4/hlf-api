package de.upb.cs.uc4.hyperledger.tests.suites

import de.upb.cs.uc4.hyperledger.tests.{CertificateAccessTests, CourseAccessTests, CourseErrorTests, InternalManagerTests, MatriculationAccessTests, MatriculationErrorTests, UserManagementTests}
import org.scalatest.Sequential

class ProductionNetworkTestSuite extends Sequential(
  new CertificateAccessTests,
  new CourseAccessTests,
  new CourseErrorTests,
  new InternalManagerTests,
  new MatriculationAccessTests,
  new MatriculationErrorTests,
  new UserManagementTests
)
