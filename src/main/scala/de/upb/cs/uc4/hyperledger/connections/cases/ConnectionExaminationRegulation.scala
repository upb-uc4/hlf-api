package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExaminationRegulationTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

case class ConnectionExaminationRegulation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionExaminationRegulationTrait {

  def getProposalAddExaminationRegulation(examinationRegulation: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("addExaminationRegulation", examinationRegulation)
  }

  def getProposalGetExaminationRegulations(namesList: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("getExaminationRegulations", namesList)
  }

  def getProposalCloseExaminationRegulation(name: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("closeExaminationRegulation", name)
  }

  def addExaminationRegulation(examinationRegulation: String): String =
    wrapSubmitTransaction(false, "addExaminationRegulation", examinationRegulation)

  def getExaminationRegulations(namesList: String): String =
    wrapEvaluateTransaction("getExaminationRegulations", namesList)

  def closeExaminationRegulation(name: String): String =
    wrapSubmitTransaction(false, "closeExaminationRegulation", name)
}
