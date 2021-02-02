package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionExamTrait extends ConnectionTrait {
  final override val contractName: String = "UC4.Exam"

  /** Retrieves a proposal for the designated query
    * Also submits approval for the query as current user (admin).
    *
    * @param examJson Information about the exam to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The Proposal requested.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddExam(certificate: String, affiliation: String = AFFILIATION, examJson: String): (String, Array[Byte])

  /** Retrieves a proposal for the designated query
    * Also submits approval for the query as current user (admin).
    *
    * @param examIds        Filter for the examIds - leave empty to ignore filter
    * @param courseIds      Filter for the courseIds - leave empty to ignore filter
    * @param lecturerIds    Filter for the lecturerIds - leave empty to ignore filter
    * @param moduleIds      Filter for the moduleIds - leave empty to ignore filter
    * @param types          Filter for the types - leave empty to ignore filter
    * @param admittableAt   Filter for the admittableUntil date - leave empty to ignore filter
    * @param droppableAt    Filter for the droppableUntil date - leave empty to ignore filter
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The Proposal requested.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetExams(certificate: String, affiliation: String = AFFILIATION, examIds:List[String],
                          courseIds: List[String], lecturerIds: List[String], moduleIds: List[String],
                          types: List[String], admittableAt: String, droppableAt: String): (String, Array[Byte])

  /** Submits the "addExam" query.
    *
    * @param examJson Information about the exam to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The final object that is present on the chain after performing the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addExam(examJson: String): String

  /** Submits the "getExams" query.
    *
    * @param examIds        Filter for the examIds - leave empty to ignore filter
    * @param courseIds      Filter for the courseIds - leave empty to ignore filter
    * @param lecturerIds    Filter for the lecturerIds - leave empty to ignore filter
    * @param moduleIds      Filter for the moduleIds - leave empty to ignore filter
    * @param types          Filter for the types - leave empty to ignore filter
    * @param admittableAt   Filter for the admittableUntil date - leave empty to ignore filter
    * @param droppableAt    Filter for the droppableUntil date - leave empty to ignore filter
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The final object that is present on the chain after performing the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getExams(examIds:List[String], courseIds: List[String], lecturerIds: List[String], moduleIds: List[String],
               types: List[String], admittableAt: String, droppableAt: String): String
}
