package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExaminationRegulationTrait

case class ConnectionExaminationRegulation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionExaminationRegulationTrait {

  def getProposalAddExaminationRegulation(certificate: String, affiliation: String, examinationRegulation: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "addExaminationRegulation", examinationRegulation)
  }

  def getProposalGetExaminationRegulations(certificate: String, affiliation: String, namesList: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "getExaminationRegulations", namesList)
  }

  def getProposalCloseExaminationRegulation(certificate: String, affiliation: String, name: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "closeExaminationRegulation", name)
  }

  def addExaminationRegulation(examinationRegulation: String): String =
    wrapSubmitTransaction(false, "addExaminationRegulation", examinationRegulation)

  def getExaminationRegulations(namesList: String): String =
    wrapEvaluateTransaction("getExaminationRegulations", namesList)

  def closeExaminationRegulation(name: String): String =
    wrapSubmitTransaction(false, "closeExaminationRegulation", name)
}
