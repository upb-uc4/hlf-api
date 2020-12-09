package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

case class ConnectionCertificate(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionCertificateTrait {

  override def getProposalAddCertificate(certificate: String, affiliation: String, enrollmentID: String, newCertificate: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "addCertificate", enrollmentID, newCertificate)
  }

  override def getProposalUpdateCertificate(certificate: String, affiliation: String, enrollmentID: String, newCertificate: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "updateCertificate", enrollmentID, newCertificate)
  }

  override def getProposalGetCertificate(certificate: String, affiliation: String, enrollmentID: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "getCertificate", enrollmentID)
  }

  override def addCertificate(enrollmentID: String, certificate: String): String =
    wrapSubmitTransaction(false, "addCertificate", enrollmentID, certificate)

  override def updateCertificate(enrollmentID: String, certificate: String): String =
    wrapSubmitTransaction(false, "updateCertificate", enrollmentID, certificate)

  override def getCertificate(enrollmentId: String): String =
    wrapEvaluateTransaction("getCertificate", enrollmentId)

  override def addOrUpdateCertificate(enrollmentID: String, enrollmentCertificate: String): String = {
    val alreadyContained = containsCertificate(enrollmentID)

    // store
    if (!alreadyContained) {
      Logger.info(s"Storing new certificate for enrollmentID: $enrollmentID")
      addCertificate(enrollmentID, enrollmentCertificate)
    }
    else {
      Logger.info(s"Updating existing certificate for enrollmentID: $enrollmentID")
      updateCertificate(enrollmentID, enrollmentCertificate)
    }
  }

  private def containsCertificate(enrollmentID: String): Boolean = {
    try {
      getCertificate(enrollmentID)
      true
    }
    catch {
      case _: Throwable => false
    }
  }
}
