package de.upb.cs.uc4.hyperledger.utilities.helper

import java.io.ByteArrayInputStream
import java.security.cert.{ CertificateFactory, X509Certificate }
import java.util.Base64

object CertificateHelper {

  def getNameFromCertificate(certificate: String): String = {
    val nameString = getCertificateInfoFromFileContent(certificate).getSubjectDN.getName.substring(3)
    var nameClean = nameString
    if (nameString.contains(",")) {
      nameClean = nameString.split(",").head
    }
    nameClean
  }

  def getCertificateInfoFromFileContent(certificate: String): X509Certificate = {
    val cf = CertificateFactory.getInstance("X.509")
    cf.generateCertificate(
      new ByteArrayInputStream(
        Base64.getDecoder.decode(
          certificate.stripPrefix("-----BEGIN CERTIFICATE-----").replaceAll("\n", "").stripSuffix("-----END CERTIFICATE-----")
        )
      )
    ).asInstanceOf[X509Certificate]
  }
}
