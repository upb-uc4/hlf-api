package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import com.google.gson.Gson
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait

import scala.jdk.CollectionConverters.SeqHasAsJava

protected[hyperledger] case class ConnectionAdmission(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionAdmissionTrait {

  override def getProposalAddAdmission(certificate: String, affiliation: String = AFFILIATION, admission: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "addAdmission", admission)
  }

  override def getProposalDropAdmission(certificate: String, affiliation: String = AFFILIATION, admissionId: String): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "dropAdmission", admissionId)
  }

  override def getProposalGetAdmissions(certificate: String, affiliation: String = AFFILIATION, enrollmentId: String = "", courseId: String = "", moduleId: String = ""): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "getAdmissions", enrollmentId, courseId, moduleId)
  }

  override def getProposalGetCourseAdmissions(certificate: String, affiliation: String = AFFILIATION, enrollmentId: String = "", courseId: String = "", moduleId: String = ""): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "getCourseAdmissions", enrollmentId, courseId, moduleId)
  }

  override def getProposalGetExamAdmissions(certificate: String, affiliation: String = AFFILIATION, admissionIds: Seq[String], enrollmentId: String, examIds: Seq[String]): (String, Array[Byte]) = {
    internalApproveAsCurrentAndGetProposalProposeTransaction(certificate, affiliation, "getExamAdmissions",
      new Gson().toJson(admissionIds.asJava),
      enrollmentId,
      new Gson().toJson(examIds.asJava))
  }

  override def addAdmission(admission: String): String =
    wrapSubmitTransaction(false, "addAdmission", admission)()

  override def dropAdmission(admissionId: String): String =
    wrapSubmitTransaction(false, "dropAdmission", admissionId)()

  override def getAdmissions(enrollmentId: String = "", courseId: String = "", moduleId: String = ""): String =
    wrapEvaluateTransaction("getAdmissions", enrollmentId, courseId, moduleId)

  override def getCourseAdmissions(enrollmentId: String = "", courseId: String = "", moduleId: String = ""): String =
    wrapEvaluateTransaction("getCourseAdmissions", enrollmentId, courseId, moduleId)

  override def getExamAdmissions(admissionIds: Seq[String], enrollmentId: String, examIds: Seq[String]): String =
    wrapEvaluateTransaction(
      "getExamAdmissions",
      new Gson().toJson(admissionIds.asJava),
      enrollmentId,
      new Gson().toJson(examIds.asJava)
    )
}
