package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

case class ConnectionMatriculation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path) extends ConnectionMatriculationTrait {
  final override val contractName: String = "UC4.MatriculationData"
  override val (contract, gateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.contractName, walletPath, networkDescriptionPath)
  override val (draftContract, draftGateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.draftContractName, walletPath, networkDescriptionPath)

  def getProposalAddMatriculationData(jSonMatriculationData: String): (Array[Byte], String) = {
    // TODO: submit only to draft-contract
    // addMatriculationData(jSonMatriculationData)
    // TODO: add error handling
    internalGetUnsignedProposal("addMatriculationData", jSonMatriculationData)
  }

  def getProposalAddEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): (Array[Byte], String) = {
    // TODO: submit only to draft-contract
    // addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String)
    // TODO: add error handling
    internalGetUnsignedProposal("addEntriesToMatriculationData", enrollmentId: String, subjectMatriculationList: String)
  }

  def getProposalUpdateMatriculationData(jSonMatriculationData: String): (Array[Byte], String) = {
    // TODO: submit only to draft-contract
    // updateMatriculationData(jSonMatriculationData)
    // TODO: add error handling
    internalGetUnsignedProposal("updateMatriculationData", jSonMatriculationData)
  }

  def getProposalGetMatriculationData(enrollmentId: String): (Array[Byte], String) = {
    // TODO: have admin check user can access info?
    // TODO: submit only to draft-contract
    // getMatriculationData(enrollmentId)
    // TODO: add error handling
    internalGetUnsignedProposal("getMatriculationData", enrollmentId)
  }

  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult(
      "addMatriculationData",
      this.internalSubmitTransaction(false, "addMatriculationData", jSonMatriculationData)
    )

  override def addEntriesToMatriculationData(enrollmentId: String, subjectMatriculationList: String): String =
    wrapTransactionResult(
      "addEntriesToMatriculationData",
      this.internalSubmitTransaction(false, "addEntriesToMatriculationData", enrollmentId, subjectMatriculationList)
    )

  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult(
      "updateMatriculationData",
      this.internalSubmitTransaction(false, "updateMatriculationData", jSonMatriculationData)
    )

  override def getMatriculationData(enrollmentId: String): String =
    wrapTransactionResult(
      "getMatriculationData",
      this.internalEvaluateTransaction("getMatriculationData", enrollmentId)
    )
}
