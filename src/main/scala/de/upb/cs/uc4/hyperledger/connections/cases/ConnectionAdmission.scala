package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait

protected[hyperledger] case class ConnectionAdmission(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionAdmissionTrait {

  override def getProposalAddAdmission(certificate: String, affiliation: String = AFFILIATION, admission: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "addAdmission", admission)
  }

  override def getProposalDropAdmission(certificate: String, affiliation: String = AFFILIATION, admissionId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "dropAdmission", admissionId)
  }

  override def getProposalGetAdmission(certificate: String, affiliation: String = AFFILIATION, enrollmentId: String = "", courseId: String = "", moduleId: String = ""): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "getAdmissions", enrollmentId, courseId, moduleId)
  }

  override def addAdmission(admission: String): String =
    wrapSubmitTransaction(false, "addAdmission", admission)

  override def dropAdmission(admissionId: String): String =
    wrapSubmitTransaction(false, "dropAdmission", admissionId)

  override def getAdmissions(enrollmentId: String = "", courseId: String = "", moduleId: String = ""): String =
    wrapSubmitTransaction(false, "getAdmissions", enrollmentId, courseId, moduleId)
}
