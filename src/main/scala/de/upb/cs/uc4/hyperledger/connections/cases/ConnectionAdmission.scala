package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait

protected[hyperledger] case class ConnectionAdmission(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionAdmissionTrait {

  override def getProposalAddAdmission(certificate: String, admission: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "addAdmission", admission)
  }

  override def getProposalDropAdmission(certificate: String, admissionId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "dropAdmission", admissionId)
  }

  override def getProposalGetAdmission(certificate: String, enrollmentId: String = "", courseId: String = "", moduleId: String = ""): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "getAdmissions", enrollmentId, courseId, moduleId)
  }

  override def addAdmission(admission: String): String =
    wrapSubmitTransaction(false, "addAdmission", admission)

  override def dropAdmission(admissionId: String): String =
    wrapSubmitTransaction(false, "dropAdmission", admissionId)

  override def getAdmissions(enrollmentId: String = "", courseId: String = "", moduleId: String = ""): String =
    wrapSubmitTransaction(false, "getAdmissions", enrollmentId, courseId, moduleId)
}
