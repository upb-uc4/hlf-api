package de.upb.cs.uc4.hyperledger.connections.traits

import java.lang.reflect.{ Field, Method }
import java.nio.charset.StandardCharsets
import java.util
import java.util.concurrent.TimeoutException

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerException, NetworkException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl, TransactionImpl }
import org.hyperledger.fabric.gateway.{ ContractException, GatewayRuntimeException, Transaction }
import org.hyperledger.fabric.protos.peer.ProposalPackage
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk._
import org.hyperledger.fabric.sdk.transaction.{ ProposalBuilder, TransactionContext }

import scala.jdk.CollectionConverters.MapHasAsJava

trait ConnectionTrait extends AutoCloseable {
  val contractName: String
  val contract: ContractImpl
  val gateway: GatewayImpl

  val draftContractName: String = "UC4.Draft"
  val draftContract: ContractImpl
  val draftGateway: GatewayImpl

  @throws[HyperledgerExceptionTrait]
  protected final def internalSubmitTransaction(transient: Boolean, transactionName: String, params: String*): Array[Byte] = {
    testParamsNull(transactionName, params: _*)
    try {
      if (transient) {
        var transMap: Map[String, Array[Byte]] = Map()
        var i = 0
        params.foreach(param => {
          transMap += i.toString -> param.toCharArray.map(_.toByte)
          i = i + 1
        })

        contract.createTransaction(transactionName).setTransient(transMap.asJava).submit()
      }
      else {
        contract.submitTransaction(transactionName, params: _*)
      }
    }
    catch {
      case ex: GatewayRuntimeException => throw NetworkException(innerException = ex)
      case ex: TimeoutException        => throw NetworkException(innerException = ex)
      case ex: Exception               => throw HyperledgerException(transactionName, ex)
    }
  }

  @throws[HyperledgerExceptionTrait]
  protected final def internalEvaluateTransaction(transactionName: String, params: String*): Array[Byte] = {
    testParamsNull(transactionName, params: _*)
    try {
      contract.evaluateTransaction(transactionName, params: _*)
    }
    catch {
      case ex: GatewayRuntimeException => throw NetworkException(innerException = ex)
      case ex: TimeoutException        => throw NetworkException(innerException = ex)
      case ex: Exception               => throw HyperledgerException(transactionName, ex)
    }
  }

  protected final def internalGetUnsignedProposal(transactionName: String, params: String*): (Array[Byte], String) = {
    val (proposal: Proposal, id: String) = this.createUnsignedTransaction(transactionName, params: _*)
    (proposal.toByteArray, id)
  }

  final def submitSignedProposal(proposalBytes: Array[Byte], signature: ByteString, transactionName: String, transactionId: String, params: String*): Array[Byte] = {
    val proposal: Proposal = Proposal.parseFrom(proposalBytes)
    internalSubmitSignedProposal(proposal, signature, transactionName, transactionId, params: _*)
  }

  private final def createUnsignedTransaction(transactionName: String, params: String*): (Proposal, String) = {
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = callPrivateMethod(transaction)("newProposalRequest")(params.toArray).asInstanceOf[TransactionProposalRequest]
    val context: TransactionContext = callPrivateMethod(contract.getNetwork.getChannel)("getTransactionContext")(request).asInstanceOf[TransactionContext]
    val proposalBuilder: ProposalBuilder = ProposalBuilder.newBuilder()
    proposalBuilder.context(context)
    proposalBuilder.request(request)
    (proposalBuilder.build(), context.getTxID)
  }

  private final def internalSubmitSignedProposal(proposal: Proposal, signature: ByteString, transactionName: String, transactionId: String, params: String*): Array[Byte] = {
    val (transaction: TransactionImpl, context, signedProposal) = createProposal(proposal, signature, transactionName, transactionId, params: _*)
    val proposalResponses = sendProposalToPeers(context, signedProposal)
    val validResponses = callPrivateMethod(transaction)("validatePeerResponses")(proposalResponses).asInstanceOf[util.Collection[ProposalResponse]]
    commitTransaction(transaction, proposalResponses, validResponses)
  }

  private def createProposal(proposal: ProposalPackage.Proposal, signature: ByteString, transactionName: String, transactionId: String, params: String*) = {
    val signedProposalBuilder: ProposalPackage.SignedProposal.Builder = ProposalPackage.SignedProposal.newBuilder
    val signedProposal: ProposalPackage.SignedProposal = signedProposalBuilder.setProposalBytes(proposal.toByteString).setSignature(signature).build
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = callPrivateMethod(transaction)("newProposalRequest")(params.toArray).asInstanceOf[TransactionProposalRequest]
    val context: TransactionContext = callPrivateMethod(contract.getNetwork.getChannel)("getTransactionContext")(request).asInstanceOf[TransactionContext]
    setPrivateField(context)("txID")(transactionId)
    context.verify(request.doVerify())
    context.setProposalWaitTime(request.getProposalWaitTime)
    setPrivateField(transaction)("transactionContext")(context)
    (transaction, context, signedProposal)
  }

  private def sendProposalToPeers(context: TransactionContext, signedProposal: ProposalPackage.SignedProposal) = {
    val channel: Channel = contract.getNetwork.getChannel
    val peers: util.Collection[Peer] = callPrivateMethod(channel)("getEndorsingPeers")().asInstanceOf[util.Collection[Peer]]
    callPrivateMethod(channel)("sendProposalToPeers")(peers, signedProposal, context).asInstanceOf[util.Collection[ProposalResponse]]
  }

  private def commitTransaction(transaction: Transaction, proposalResponses: util.Collection[ProposalResponse], validResponses: util.Collection[ProposalResponse]) = {
    try callPrivateMethod(transaction)("commitTransaction")(validResponses).asInstanceOf[Array[Byte]]
    catch {
      case e: ContractException =>
        e.setProposalResponses(proposalResponses)
        throw e
    }
    //} catch {
    //  case e@(_: InvalidArgumentException | _: ProposalException | _: ServiceDiscoveryException) =>
    //   throw new GatewayRuntimeException(e)
    //}
  }

  private def callPrivateMethod(instance: AnyRef)(methodName: String)(args: AnyRef*): AnyRef = {
    def _parents: LazyList[Class[_]] = LazyList(instance.getClass) #::: _parents.map(_.getSuperclass)
    val parents: List[Class[_]] = _parents.takeWhile(_ != null).toList
    val methods: List[Method] = parents.flatMap(_.getDeclaredMethods)
    val method: Method = methods.find(method =>
      method.getName == methodName && method.getParameterCount == args.length)
      .getOrElse(throw new IllegalArgumentException("Method " + methodName + " not found"))
    method.setAccessible(true)
    try {
      method.invoke(instance, args: _*)
    } catch {
      case ex: Throwable => throw Logger.err("Exception on invocation: ", ex)
    }
  }

  private def setPrivateField(instance: AnyRef)(fieldName: String)(arg: AnyRef): Unit = {
    def _parents: LazyList[Class[_]] = LazyList(instance.getClass) #::: _parents.map(_.getSuperclass)
    val parents: List[Class[_]] = _parents.takeWhile(_ != null).toList
    val fields: List[Field] = parents.flatMap(_.getDeclaredFields)
    val field: Field = fields.find(_.getName == fieldName)
      .getOrElse(throw new IllegalArgumentException("Method " + fieldName + " not found"))
    field.setAccessible(true)
    field.set(instance, arg)
  }

  /** Since the chain returns bytes, we need to convert them to a readable Result.
    *
    * @param result Bytes containing a result from a chaincode transaction.
    * @return Result as a String.
    */
  protected final def convertTransactionResult(result: Array[Byte]): String = new String(result, StandardCharsets.UTF_8)

  /** Wraps the chaincode query result bytes.
    * Translates the byte-array to a string and throws an error if said string is not empty
    *
    * @param result input byte-array to translate
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  protected final def wrapTransactionResult(transactionName: String, result: Array[Byte]): String = {
    val resultString = convertTransactionResult(result)
    if (containsError(resultString)) throw TransactionException(transactionName, resultString)
    else resultString
  }

  /** Evaluates whether a transactionResult contains a "detailedError" or a "genericError"
    *
    * @param result result of a chaincode transaction
    * @return true if the result contains error information conforming to API-standards
    */
  private def containsError(result: String): Boolean = result.contains("{\"type\":") && result.contains("\"title\":")

  final override def close(): Unit = this.gateway.close()

  /** Checks if the transaction params are null.
    *
    * @param transactionName transactionId causing the error.
    * @param params parameters to check
    * @throws TransactionException if a parameter is null.
    */
  @throws[TransactionExceptionTrait]
  private def testParamsNull(transactionName: String, params: String*): Unit = {
    params.foreach(param => if (param == null) throw TransactionException.CreateUnknownException(transactionName, "A parameter was null."))
  }
}
