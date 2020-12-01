package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionApprovalsTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.utilities.helper.TransactionHelper

protected[hyperledger] case class ConnectionAdmission(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionAdmissionTrait {

  override def getProposalAddAdmission(admission: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("addAdmission", admission)
  }

  override def getProposalDropAdmission(admissionId: String): Array[Byte] =
    {
      // TODO: add error handling
      internalGetUnsignedProposal("dropAdmission", admissionId)
    }

  override def getProposalGetAdmission(enrollmentId: String, courseId: String, moduleId: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("getAdmissions", enrollmentId, courseId, moduleId)
  }

  override def addAdmission(admission: String): String =
    wrapSubmitTransaction(false, "addAdmission", admission)

  override def dropAdmission(admissionId: String): String =
    wrapSubmitTransaction(false, "dropAdmission", admissionId)

  override def getAdmissions(enrollmentId: String, courseId: String, moduleId: String): String =
    wrapSubmitTransaction(false, "getAdmissions", enrollmentId, courseId, moduleId)
}
