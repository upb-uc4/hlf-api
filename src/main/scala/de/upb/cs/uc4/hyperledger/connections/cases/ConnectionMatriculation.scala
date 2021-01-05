package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait

case class ConnectionMatriculation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionMatriculationTrait {
  final override val contractName: String = "UC4.MatriculationData"

  def getProposalAddMatriculationData(certificate: String, jSonMatriculationData: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "addMatriculationData", jSonMatriculationData)
  }

  def getProposalAddEntriesToMatriculationData(certificate: String, enrollmentId: String, subjectMatriculationList: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "addEntriesToMatriculationData", enrollmentId: String, subjectMatriculationList: String)
  }

  def getProposalUpdateMatriculationData(certificate: String, jSonMatriculationData: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "updateMatriculationData", jSonMatriculationData)
  }

  def getProposalGetMatriculationData(certificate: String, enrollmentId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, "getMatriculationData", enrollmentId)
  }

  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "addMatriculationData", jSonMatriculationData)

  override def addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): String =
    wrapSubmitTransaction(false, "addEntriesToMatriculationData", enrollmentId, subjectMatriculationList)

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "updateMatriculationData", jSonMatriculationData)

  override def getMatriculationData(enrollmentId: String): String =
    wrapEvaluateTransaction("getMatriculationData", enrollmentId)
}
