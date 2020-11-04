package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionExaminationRegulationTrait extends ConnectionTrait {

  /** Retrieves a proposal for the designated query
    * Also submits approval for the query as current user (admin).
    *
    * @param examinationRegulation Information about the examination regulation to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The Proposal requested.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddExaminationRegulation(examinationRegulation: String): Array[Byte]

  /** Retrieves a proposal for the designated query
    * Also submits approval for the query as current user (admin).
    *
    * @param namesList List of Examination Regulations to get.
    *                  Empty name list will return all existing examination regulations.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The Proposal requested.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetExaminationRegulations(namesList: String): Array[Byte]

  /** Retrieves a proposal for the designated query
    * Also submits approval for the query as current user (admin).
    *
    * @param name identifier of the examination regulation to close.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The Proposal requested.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalCloseExaminationRegulation(name: String): Array[Byte]

  /** Submits the "addExaminationRegulation" query.
    *
    * @param examinationRegulation Information about the examination regulation to add.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The final object that is present on the chain after performing the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addExaminationRegulation(examinationRegulation: String): String

  /** Submits the "getExaminationRegulations" query.
    *
    * @param namesList List of Examination Regulations to get.
    *                  Empty name list will return all existing examination regulations.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The final object that is present on the chain after performing the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getExaminationRegulations(namesList: String): String

  /** Executes the "closeExaminationRegulation" query.
    *
    * @param name identifier of the examination regulation to close.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return The final object that is present on the chain after performing the transaction.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def closeExaminationRegulation(name: String): String
}
