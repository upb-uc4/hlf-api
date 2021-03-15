package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait

case class ConnectionMatriculation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionMatriculationTrait {
  final override val contractName: String = "UC4.MatriculationData"

  def getProposalAddMatriculationData(certificate: String, affiliation: String = AFFILIATION, jSonMatriculationData: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "addMatriculationData", jSonMatriculationData)
  }

  def getProposalAddEntriesToMatriculationData(certificate: String, affiliation: String = AFFILIATION, enrollmentId: String, subjectMatriculationList: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "addEntriesToMatriculationData", enrollmentId: String, subjectMatriculationList: String)
  }

  def getProposalUpdateMatriculationData(certificate: String, affiliation: String = AFFILIATION, jSonMatriculationData: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "updateMatriculationData", jSonMatriculationData)
  }

  def getProposalGetMatriculationData(certificate: String, affiliation: String = AFFILIATION, enrollmentId: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "getMatriculationData", enrollmentId)
  }

  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "addMatriculationData", jSonMatriculationData)()

  override def addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): String =
    wrapSubmitTransaction(false, "addEntriesToMatriculationData", enrollmentId, subjectMatriculationList)()

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapSubmitTransaction(false, "updateMatriculationData", jSonMatriculationData)()

  override def getMatriculationData(enrollmentId: String): String =
    wrapEvaluateTransaction("getMatriculationData", enrollmentId)
}
