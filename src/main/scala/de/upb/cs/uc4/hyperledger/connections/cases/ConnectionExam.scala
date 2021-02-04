package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import com.google.gson.Gson
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExamTrait

import scala.jdk.CollectionConverters.SeqHasAsJava

case class ConnectionExam(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionExamTrait {

  def getProposalAddExam(certificate: String, affiliation: String = AFFILIATION, examJson: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "addExam", examJson)
  }

  def getProposalGetExams(certificate: String, affiliation: String = AFFILIATION, examIds: Seq[String],
      courseIds: Seq[String], lecturerIds: Seq[String], moduleIds: Seq[String],
      types: Seq[String], admittableAt: String, droppableAt: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "getExams",
      new Gson().toJson(examIds.asJava),
      new Gson().toJson(courseIds.asJava),
      new Gson().toJson(lecturerIds.asJava),
      new Gson().toJson(moduleIds.asJava),
      new Gson().toJson(types.asJava),
      admittableAt, droppableAt)
  }

  def addExam(examJson: String): String =
    wrapSubmitTransaction(false, "addExam", examJson)()

  def getExams(examIds: Seq[String], courseIds: Seq[String], lecturerIds: Seq[String], moduleIds: Seq[String],
      types: Seq[String], admittableAt: String, droppableAt: String): String =
    wrapEvaluateTransaction(
      "getExams",
      new Gson().toJson(examIds.asJava),
      new Gson().toJson(courseIds.asJava),
      new Gson().toJson(lecturerIds.asJava),
      new Gson().toJson(moduleIds.asJava),
      new Gson().toJson(types.asJava),
      admittableAt, droppableAt
    )
}
