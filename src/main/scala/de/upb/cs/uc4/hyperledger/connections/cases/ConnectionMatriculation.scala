package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait

case class ConnectionMatriculation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionMatriculationTrait {
  final override val contractName: String = "UC4.MatriculationData"

  def getProposalAddMatriculationData(certificate: String, affiliation: String, jSonMatriculationData: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "addMatriculationData", jSonMatriculationData)
  }

  def getProposalAddEntriesToMatriculationData(certificate: String, affiliation: String, enrollmentId: String, subjectMatriculationList: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "addEntriesToMatriculationData", enrollmentId: String, subjectMatriculationList: String)
  }

  def getProposalUpdateMatriculationData(certificate: String, affiliation: String, jSonMatriculationData: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "updateMatriculationData", jSonMatriculationData)
  }

  def getProposalGetMatriculationData(certificate: String, affiliation: String, enrollmentId: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "getMatriculationData", enrollmentId)
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
