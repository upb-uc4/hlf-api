package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

case class ConnectionCertificate(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionCertificateTrait {
  final override val contractName: String = "UC4.Certificate"

  override def getProposalAddCertificate(enrollmentID: String, certificate: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("addCertificate", enrollmentID, certificate)
  }

  override def getProposalUpdateCertificate(enrollmentID: String, certificate: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("updateCertificate", enrollmentID, certificate)
  }

  override def getProposalGetCertificate(enrollmentID: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("getCertificate", enrollmentID)
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
