package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait

case class ConnectionCertificate(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionCertificateTrait {

  override def getProposalAddCertificate(certificate: String, affiliation: String = AFFILIATION, enrollmentID: String, newCertificate: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "addCertificate", enrollmentID, newCertificate)
  }

  override def getProposalUpdateCertificate(certificate: String, affiliation: String = AFFILIATION, enrollmentID: String, newCertificate: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "updateCertificate", enrollmentID, newCertificate)
  }

  override def getProposalGetCertificate(certificate: String, affiliation: String = AFFILIATION, enrollmentID: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "getCertificate", enrollmentID)
  }

  override def addCertificate(enrollmentID: String, certificate: String): String =
    wrapSubmitTransaction(false, "addCertificate", enrollmentID, certificate)()

  override def updateCertificate(enrollmentID: String, certificate: String): String =
    wrapSubmitTransaction(false, "updateCertificate", enrollmentID, certificate)()

  override def getCertificate(enrollmentId: String): String =
    wrapEvaluateTransaction("getCertificate", enrollmentId)
}
