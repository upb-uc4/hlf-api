package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import com.google.gson.Gson
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExamResultTrait

import scala.jdk.CollectionConverters.SeqHasAsJava

case class ConnectionExamResult(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionExamResultTrait {

  def getProposalAddExamResult(certificate: String, affiliation: String = AFFILIATION, examResultJson: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "addExamResult", examResultJson)
  }

  def getProposalGetExamResultEntries(certificate: String, affiliation: String = AFFILIATION,
      enrollmentId: String, examIds: Seq[String]): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "getExamResultEntries",
      enrollmentId,
      new Gson().toJson(examIds.asJava))
  }

  def addExamResult(examResultJson: String): String =
    wrapSubmitTransaction(false, "addExamResult", examResultJson)()

  def getExamResultEntries(enrollmentId: String, examIds: Seq[String]): String =
    wrapEvaluateTransaction("getExamResultEntries", enrollmentId, new Gson().toJson(examIds.asJava))
}
