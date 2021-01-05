package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

case class ConnectionCertificate(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionCertificateTrait {

  override def getProposalAddCertificate(certificate: String, enrollmentID: String, newCertificate: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "addCertificate", enrollmentID, newCertificate)
  }

  override def getProposalUpdateCertificate(certificate: String, enrollmentID: String, newCertificate: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "updateCertificate", enrollmentID, newCertificate)
  }

  override def getProposalGetCertificate(certificate: String, enrollmentID: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "getCertificate", enrollmentID)
  }

  override def addCertificate(enrollmentID: String, certificate: String): String =
    wrapSubmitTransaction(false, "addCertificate", enrollmentID, certificate)

  override def updateCertificate(enrollmentID: String, certificate: String): String =
    wrapSubmitTransaction(false, "updateCertificate", enrollmentID, certificate)

  override def getCertificate(enrollmentId: String): String =
    wrapEvaluateTransaction("getCertificate", enrollmentId)
}
