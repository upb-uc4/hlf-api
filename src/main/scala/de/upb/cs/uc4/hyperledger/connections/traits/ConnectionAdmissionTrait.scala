package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionAdmissionTrait extends ConnectionTrait {
  final override val contractName: String = "UC4.Admission"

  /** Retrieves a proposal for the designated query
    * Also submits the "addAdmission" query as current user (admin).
    *
    * @param admission Information about the admission.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddAdmission(certificate: String, affiliation: String = AFFILIATION, admission: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the query as current user (admin).
    *
    * @param admissionId admissionId to drop.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalDropAdmission(certificate: String, affiliation: String = AFFILIATION, admissionId: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the query as current user (admin).
    *
    * @param enrollmentId enrollmentId to filter for.
    * @param courseId     courseId to filter for.
    * @param moduleId     moduleId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetAdmissions(certificate: String, affiliation: String = AFFILIATION, enrollmentId: String = "", courseId: String = "", moduleId: String = ""): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the query as current user (admin).
    *
    * @param enrollmentId enrollmentId to filter for.
    * @param courseId     courseId to filter for.
    * @param moduleId     moduleId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetCourseAdmissions(certificate: String, affiliation: String = AFFILIATION, enrollmentId: String = "", courseId: String = "", moduleId: String = ""): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits the query as current user (admin).
    *
    * @param admissionIds admissionIds to filter for.
    * @param enrollmentId enrollmentId to filter for.
    * @param examIds examIds to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetExamAdmissions(certificate: String, affiliation: String = AFFILIATION, admissionIds: List[String], enrollmentId: String, examIds: List[String]): (String, Array[Byte])

  /** Submits the "addAdmission" query.
    *
    * @param admission Information about the admission.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return ledger state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addAdmission(admission: String): String

  /** Submits the "dropAdmission" query.
    *
    * @param admissionId admissionId to drop.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def dropAdmission(admissionId: String): String

  /** Submits the "getAdmissions" query.
    *
    * @param enrollmentId enrollmentId to filter for.
    * @param courseId     courseId to filter for.
    * @param moduleId     moduleId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return list of admissions matching the filters.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getAdmissions(enrollmentId: String = "", courseId: String = "", moduleId: String = ""): String


  /** Submits the "getCourseAdmissions" query.
    *
    * @param enrollmentId enrollmentId to filter for.
    * @param courseId     courseId to filter for.
    * @param moduleId     moduleId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return list of admissions matching the filters.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getCourseAdmissions(enrollmentId: String = "", courseId: String = "", moduleId: String = ""): String


  /** Submits the "getExamAdmissions" query.
    *
    * @param admissionIds admissionIds to filter for.
    * @param enrollmentId enrollmentId to filter for.
    * @param examIds examIds to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return list of admissions matching the filters.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getExamAdmissions(admissionIds: List[String], enrollmentId: String, examIds: List[String]): String
}
