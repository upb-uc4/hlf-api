package de.upb.cs.uc4.hyperledger.tests.testUtil

import java.security.cert.X509Certificate
import java.util.Base64

import de.upb.cs.uc4.hyperledger.utilities.helper.ReflectionHelper
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._

object TestHelperCrypto {
  def toPemString(certificate: X509Certificate): String = {
    import sun.security.provider.X509Factory
    s"${X509Factory.BEGIN_CERT}\n${Base64.getEncoder.encodeToString(certificate.getEncoded).replaceAll(".{64}", "$0\n")}\n${X509Factory.END_CERT}\n"
  }

  def getCryptoPrimitives: CryptoPrimitives = {
    val crypto: CryptoPrimitives = new CryptoPrimitives()
    val securityLevel: Integer = 256
    ReflectionHelper.safeCallPrivateMethod(crypto)("setSecurityLevel")(securityLevel)
    crypto
  }
}
