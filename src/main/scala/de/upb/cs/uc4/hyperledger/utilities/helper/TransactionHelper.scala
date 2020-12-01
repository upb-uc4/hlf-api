package de.upb.cs.uc4.hyperledger.utilities.helper

import java.io.File
import java.lang.String.format
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.PrivateKey
import java.util
import java.util.Collections
import java.util.concurrent.{CompletableFuture, TimeUnit, TimeoutException}
import com.google.gson.Gson
import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionApprovalsTrait, ConnectionTrait}
import org.hyperledger.fabric.gateway.{ContractException, GatewayRuntimeException, Identities}
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser
import org.hyperledger.fabric.gateway.impl.{ContractImpl, TimePeriod, TransactionImpl}
import org.hyperledger.fabric.gateway.spi.CommitHandler
import org.hyperledger.fabric.protos.common.Common
import org.hyperledger.fabric.protos.common.Common.{Envelope, Payload, Status}
import org.hyperledger.fabric.protos.orderer.Ab.BroadcastResponse
import org.hyperledger.fabric.protos.peer.{Chaincode, ProposalPackage, ProposalResponsePackage}
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ChaincodeAction, ChaincodeProposalPayload, Proposal, SignedProposal}
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage.ProposalResponsePayload
import org.hyperledger.fabric.protos.peer.TransactionPackage.{ChaincodeActionPayload, Transaction}
import org.hyperledger.fabric.sdk.Channel.NOfEvents
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException
import org.hyperledger.fabric.sdk.helper.Config
import org.hyperledger.fabric.sdk.{BlockEvent, Channel, HFClient, NetworkConfig, Orderer, Peer, ProposalResponse, SDKUtils, TransactionProposalRequest, User}
import org.hyperledger.fabric.sdk.identity.X509Enrollment
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.hyperledger.fabric.sdk.transaction.{ProposalBuilder, TransactionBuilder, TransactionContext}

import scala.jdk.CollectionConverters._

protected[hyperledger] object TransactionHelper {

  def getParametersFromApprovalProposal(proposal: Proposal): (String, String, Array[String]) = {
    // read transaction info
    val proposalParameters = TransactionHelper.getTransactionParamsFromProposal(proposal)
    val proposalContractName = proposalParameters.head
    val transactionName = proposalParameters.tail.head
    val paramsGson = proposalParameters.tail.tail.head
    println("GSON:::: " + paramsGson)
    val params = new Gson().fromJson[Array[String]](paramsGson, classOf[Array[String]])
    (proposalContractName, transactionName, params)
  }

  def getParametersFromTransactionPayload(payload: Payload): (String, Array[String]) = {
    val chaincodeTransactionPayload: ChaincodeActionPayload = getChaincodeActionPayloadFromTransactionPayload(payload)
    val chaincodeProposalPayload: ChaincodeProposalPayload = ChaincodeProposalPayload.parseFrom(chaincodeTransactionPayload.getChaincodeProposalPayload)
    val args: Seq[String] = getArgsFromChaincodeProposalPayload(chaincodeProposalPayload)
    (args.head, args.tail.toArray)
  }

  def getChaincodeActionPayloadFromTransactionPayload(payload: Payload): ChaincodeActionPayload = {
    val transaction = Transaction.parseFrom(payload.getData)
    ChaincodeActionPayload.parseFrom(transaction.getActions(0).getPayload)
  }

  def getApprovalTransactionFromParameters(contractName: String, transactionName: String, params: Array[String]): Seq[String] = {
    val jsonParams = new Gson().toJson(params)
    val info = List[String](contractName, transactionName, jsonParams)
    Logger.info(s"PREPARE APPROVAL:: ${info.foldLeft("")((A, B) => A + "::" + B)}")
    info
  }

  def createApprovalTransactionInfo(approvalContract: ContractImpl, contractName: String, transactionName: String, params: Array[String], transactionId: Option[String]): (TransactionImpl, TransactionContext, TransactionProposalRequest) = {
    val approvalParams: Seq[String] = getApprovalTransactionFromParameters(contractName, transactionName, params)
    createTransactionInfo(approvalContract, "approveTransaction", approvalParams.toArray, transactionId)
  }

  def createTransactionInfo(contract: ContractImpl, transactionName: String, params: Array[String], transactionId: Option[String]): (TransactionImpl, TransactionContext, TransactionProposalRequest) = {
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = ReflectionHelper.safeCallPrivateMethod(transaction)("newProposalRequest")(params).asInstanceOf[TransactionProposalRequest]
    val context: TransactionContext = request.getTransactionContext.get()
    if (transactionId.isDefined) ReflectionHelper.setPrivateField(context)("txID")(transactionId.get)
    if (request.getTransientMap != null) transaction.setTransient(request.getTransientMap)
    context.verify(request.doVerify())
    context.setProposalWaitTime(request.getProposalWaitTime)
    ReflectionHelper.setPrivateField(transaction)("transactionContext")(context)
    (transaction, context, request)
  }

  def getTransactionIdFromProposal(proposal: Proposal): String = {
    getTransactionIdFromHeader(Common.Header.parseFrom(proposal.getHeader))
  }

  def getTransactionIdFromPayload(payload: Payload): String = {
    getTransactionIdFromHeader(payload.getHeader)
  }

  def getTransactionIdFromHeader(header: Common.Header): String = {
    val channelHeader = Common.ChannelHeader.parseFrom(header.getChannelHeader)
    val transactionId = channelHeader.getTxId
    transactionId
  }

  def getTransactionNameFromProposal(proposal: Proposal): String = {
    val args = getArgsFromProposal(proposal)
    val fcnName: String = args.head
    getTransactionNameFromFcn(fcnName)
  }

  def getTransactionParamsFromProposal(proposal: Proposal): Seq[String] = {
    val args = getArgsFromProposal(proposal)
    val params = args.tail
    params
  }

  def getTransactionNameFromFcn(fcn: String): String = fcn.substring(fcn.indexOf(":") + 1)

  private def getArgsFromProposal(proposal: Proposal): Seq[String] = {
    val payloadBytes: ByteString = proposal.getPayload
    val payload: ChaincodeProposalPayload = ProposalPackage.ChaincodeProposalPayload.parseFrom(payloadBytes)
    getArgsFromChaincodeProposalPayload(payload)
  }

  private def getArgsFromChaincodeProposalPayload(payload: ChaincodeProposalPayload): Seq[String] = {
    val invocationSpec: Chaincode.ChaincodeInvocationSpec = Chaincode.ChaincodeInvocationSpec.parseFrom(payload.getInput)
    val chaincodeInput = invocationSpec.getChaincodeSpec.getInput
    val args: Array[ByteString] = chaincodeInput.getArgsList.asScala.toArray
    args.map[String]((b: ByteString) => new String(b.toByteArray, StandardCharsets.UTF_8)).toList
  }

  def createSignedProposal(approvalConnection: ConnectionApprovalsTrait, proposal: ProposalPackage.Proposal, signature: ByteString): (TransactionImpl, TransactionContext, SignedProposal) = {
    val transactionId: String = TransactionHelper.getTransactionIdFromProposal(proposal)
    val transactionName: String = TransactionHelper.getTransactionNameFromProposal(proposal)
    val params: Seq[String] = TransactionHelper.getTransactionParamsFromProposal(proposal)

    val signedProposalBuilder: SignedProposal.Builder = SignedProposal.newBuilder
      .setProposalBytes(proposal.toByteString)
      .setSignature(signature)
    val signedProposal: SignedProposal = signedProposalBuilder.build

    val (transaction, context, request) = TransactionHelper.createTransactionInfo(approvalConnection.contract, transactionName, params.toArray, Some(transactionId))

    (transaction, context, signedProposal)
  }

  /**
    * TODO remove, for this moethod is likely unnecessary
    * @param certificate
    * @param userAffiliation
    * @param chaincodeName
    * @param channelName
    * @param function
    * @param networkDescriptionPath
    * @param args
    * @return
    */
  def getUnsignedProposalNew(
                              certificate: String,
                              userAffiliation: String,
                              chaincodeName: String,
                              channelName: String,
                              function: String,
                              networkDescriptionPath: Path,
                              args: String*): Proposal = {
    val enrollment: X509Enrollment = new X509Enrollment(new PrivateKey {
      override def getAlgorithm: String = null
      override def getFormat: String = null
      override def getEncoded: Array[Byte] = null
    }, certificate)
    val user: User = new GatewayUser("gateway", userAffiliation, enrollment);
    // val user: User = new GatewayUser(argEnrollmentId, testAffiliation, new X509Enrollment(adminIdentity.getPrivateKey, Identities.toPemString(adminIdentity.getCertificate)))
    val request = TransactionProposalRequest.newInstance(user)
    request.setChaincodeName(chaincodeName)
    request.setFcn(function)
    request.setArgs(args: _*)
    val networkConfigFile: File = networkDescriptionPath.toFile()
    val networkConfig: NetworkConfig = NetworkConfig.fromYamlFile(networkConfigFile)
    val hfClient: HFClient = HFClient.createNewInstance()
    val crypto: CryptoPrimitives = new CryptoPrimitives()
    val securityLevel: Integer = 256
    ReflectionHelper.safeCallPrivateMethod(crypto)("setSecurityLevel")(securityLevel)
    // TODO use get- and set properties
    hfClient.setCryptoSuite(crypto)
    hfClient.setUserContext(user)
    val channelObj: Channel = hfClient.loadChannelFromConfig(channelName, networkConfig)
    val ctx: TransactionContext = new TransactionContext(channelObj, user, crypto)
    val chaincodeId: Chaincode.ChaincodeID = Chaincode.ChaincodeID.newBuilder().setName(chaincodeName).build()
    ProposalBuilder.newBuilder().context(ctx).request(request).chaincodeID(chaincodeId).build()
  }

  def setTransactionSignature(transactionPayload: ByteString, signature: Array[Byte]): Envelope = {
    Envelope.newBuilder.setPayload(transactionPayload).setSignature(ByteString.copyFrom(signature)).build
  }

  def internalGetTransactionPayload(
                       channel: Channel,
                       proposalResponses: util.Collection[ProposalResponse],
                       transactionOptions: Channel.TransactionOptions
                     ): (Payload, String) = {
      if (null == transactionOptions) throw new InvalidArgumentException("Parameter transactionOptions can't be null")
      ReflectionHelper.safeCallPrivateMethod(channel)("checkChannelState")()
      if (null == proposalResponses) throw new InvalidArgumentException("sendTransaction proposalResponses was null")
      // make certain we have our own copy
      if (ReflectionHelper.getPrivateField(channel)("config")().asInstanceOf[Config].getProposalConsistencyValidation) {
        val invalid = new util.HashSet[ProposalResponse]
        val consistencyGroups = SDKUtils.getProposalConsistencySets(proposalResponses, invalid).size
        if (consistencyGroups != 1 || !invalid.isEmpty) throw new IllegalArgumentException(format("The proposal responses have %d inconsistent groups with %d that are invalid." + " Expected all to be consistent and none to be invalid.", consistencyGroups, invalid.size))
      }
      val ed = new util.LinkedList[ProposalResponsePackage.Endorsement]
      var proposal: Proposal = null
      var proposalResponsePayload: ByteString = null
      var proposalTransactionID: String = null
      var transactionContext: TransactionContext = null
      // import scala.collection.JavaConversions._
      proposalResponses.forEach(
        (sdkProposalResponse: ProposalResponse) => {
          ed.add(sdkProposalResponse.getProposalResponse.getEndorsement)
          if (proposal == null) {
            proposal = sdkProposalResponse.getProposal
            proposalTransactionID = sdkProposalResponse.getTransactionID
            if (proposalTransactionID == null) throw new InvalidArgumentException("Proposals with missing transaction ID")
            proposalResponsePayload = sdkProposalResponse.getProposalResponse.getPayload
            if (proposalResponsePayload == null) throw new InvalidArgumentException("Proposals with missing payload.")
            transactionContext = ReflectionHelper.safeCallPrivateMethod(sdkProposalResponse)("getTransactionContext")().asInstanceOf[TransactionContext]
            if (transactionContext == null) throw new InvalidArgumentException("Proposals with missing transaction context.")
          }
          else {
            val transactionID = sdkProposalResponse.getTransactionID
            if (transactionID == null) throw new InvalidArgumentException("Proposals with missing transaction id.")
            if (!(proposalTransactionID == transactionID)) throw new InvalidArgumentException(format("Proposals with different transaction IDs %s,  and %s", proposalTransactionID, transactionID))
          }
        }
      )
      val transactionBuilder = TransactionBuilder.newBuilder
    (transactionBuilder.chaincodeProposal(proposal).endorsements(ed).proposalResponsePayload(proposalResponsePayload).build, proposalTransactionID)
    }

  def internalSendTransaction(
                       channel: Channel,
                       signature: Array[Byte],
                       transactionOptions: Channel.TransactionOptions,
                       proposalTransactionID: String,
                       transactionPayload: ByteString
                     ): CompletableFuture[BlockEvent#TransactionEvent] = {
    try {
      val orderers = if (ReflectionHelper.getPrivateField(transactionOptions)("orderers")() != null) ReflectionHelper.getPrivateField(transactionOptions)("orderers")().asInstanceOf[util.List[Orderer]]
      else new util.ArrayList[Orderer](channel.getOrderers())
      val shuffeledOrderers: util.ArrayList[Orderer] = new util.ArrayList[Orderer](orderers)
      if (ReflectionHelper.getPrivateField(transactionOptions)("shuffleOrders")().asInstanceOf[Boolean]) Collections.shuffle(shuffeledOrderers)
      val transactionEnvelope = setTransactionSignature(transactionPayload, signature)
      var nOfEvents = ReflectionHelper.getPrivateField(transactionOptions)("nOfEvents")().asInstanceOf[NOfEvents]
      if (nOfEvents == null) {
        nOfEvents = NOfEvents.createNofEvents
        val eventingPeers = ReflectionHelper.safeCallPrivateMethod(channel)("getEventingPeers")().asInstanceOf[util.Collection[Peer]]
        var anyAdded = false
        if (!eventingPeers.isEmpty) {
          anyAdded = true
          nOfEvents.addPeers(eventingPeers)
        }
        if (!anyAdded) nOfEvents = NOfEvents.createNoEvents
      }
      else if (nOfEvents ne NOfEvents.nofNoEvents) {
        val issues = new StringBuilder(100)
        val eventingPeers = ReflectionHelper.safeCallPrivateMethod(channel)("getEventingPeers")().asInstanceOf[util.Collection[Peer]]
        ReflectionHelper.safeCallPrivateMethod(nOfEvents)("unSeenPeers")().asInstanceOf[util.Collection[Peer]].forEach((peer: Peer) => {
          def foo(peer: Peer) = if (ReflectionHelper.safeCallPrivateMethod(peer)("getChannel")() ne this) issues.append(format("Peer %s added to NOFEvents does not belong this channel. ", peer.getName))
          else if (!eventingPeers.contains(peer)) issues.append(format("Peer %s added to NOFEvents is not a eventing Peer in this channel. ", peer.getName))

          foo(peer)
        })
        if (ReflectionHelper.safeCallPrivateMethod(nOfEvents)("unSeenPeers")().asInstanceOf[util.Collection[Peer]].isEmpty) issues.append("NofEvents had no added  Peer eventing services.")
        val foundIssues = issues.toString
        if (!foundIssues.isEmpty) throw new InvalidArgumentException(foundIssues)
      }
      val replyonly = (nOfEvents eq NOfEvents.nofNoEvents) || ReflectionHelper.safeCallPrivateMethod(channel)("getEventingPeers")().asInstanceOf[util.Collection[Peer]].isEmpty
      var sret: CompletableFuture[BlockEvent#TransactionEvent] = null
      if (replyonly) { //If there are no eventsto complete the future, complete it
        // immediately but give no transaction event
        //logger.debug(format("Completing transaction id %s immediately no peer eventing services found in channel %s.", proposalTransactionID, name))
        sret = new CompletableFuture[BlockEvent#TransactionEvent]
      }
      else sret = ReflectionHelper.safeCallPrivateMethod(channel)("registerTxListener")(proposalTransactionID, nOfEvents, ReflectionHelper.getPrivateField(transactionOptions)("failFast")()).asInstanceOf[CompletableFuture[BlockEvent#TransactionEvent]]
      //logger.debug(format("Channel %s sending transaction to orderer(s) with TxID %s ", name, proposalTransactionID))
      var success: Boolean = false
      var lException: Exception = null // Save last exception to report to user .. others are just logged.
      var resp: BroadcastResponse = null
      var failed: Orderer = null
      //import scala.collection.JavaConversions._
      shuffeledOrderers.forEach((orderer: Orderer) => {
        //if (failed != null) logger.warn(format("Channel %s  %s failed. Now trying %s.", name, failed, orderer))
        failed = orderer
        try {
          //if (null != diagnosticFileDumper) logger.trace(format("Sending to channel %s, orderer: %s, transaction: %s", name, orderer.getName, diagnosticFileDumper.createDiagnosticProtobufFile(transactionEnvelope.toByteArray)))
          resp = ReflectionHelper.safeCallPrivateMethod(orderer)("sendTransaction")(transactionEnvelope).asInstanceOf[BroadcastResponse]
          lException = null // no longer last exception .. maybe just failed.

          if (resp.getStatus eq Status.SUCCESS) {
            success = true
            //break //todo: break is not supported
          }
          //else logger.warn(format("Channel %s %s failed. Status returned %s", name, orderer, getRespData(resp)))
        } catch {
          case e: Exception =>
            var emsg = format("Channel %s unsuccessful sendTransaction to orderer %s (%s)", channel.getName(), orderer.getName, orderer.getUrl)
            if (resp != null) emsg = format("Channel %s unsuccessful sendTransaction to orderer %s (%s).  %s", channel.getName, orderer.getName, orderer.getUrl, ReflectionHelper.safeCallPrivateMethod(channel)("getRespData")(resp).asInstanceOf[String])
            //logger.error(emsg)
            lException = new Exception(emsg, e)
        }
      }
      )
      if (success) {
        //logger.debug(format("Channel %s successful sent to Orderer transaction id: %s", name, proposalTransactionID))
        if (replyonly) sret.complete(null) // just say we're done.
        return sret
      }
      else {
        val emsg = format("Channel %s failed to place transaction %s on Orderer. Cause: UNSUCCESSFUL. %s", channel.getName, proposalTransactionID, ReflectionHelper.safeCallPrivateMethod(channel)("getRespData")(resp).asInstanceOf[String])
        ReflectionHelper.safeCallPrivateMethod(channel)("unregisterTxListener")(proposalTransactionID)
        val ret = new CompletableFuture[BlockEvent#TransactionEvent]
        ret.completeExceptionally(if (lException != null) new Exception(emsg, lException)
        else new Exception(emsg))
        return ret
      }
    } catch {
      case e: Exception =>
        val future = new CompletableFuture[BlockEvent#TransactionEvent]
        future.completeExceptionally(e)
        return future
    }
  }

  def getTransaction (
                       validResponses: util.Collection[ProposalResponse],
                       channelObj: Channel
                     ): (Payload, String) = {
      val transactionOptions: Channel.TransactionOptions = Channel.TransactionOptions.createTransactionOptions()
        .nOfEvents(Channel.NOfEvents.createNoEvents()) // Disable default commit wait behaviour
      internalGetTransactionPayload(channelObj, validResponses, transactionOptions)
  }

  def sendTransaction(
                       connection: ConnectionTrait,
                       channel: String,
                       ctx: TransactionContext,
                       channelObj: Channel,
                       transactionPayloadBytes: ByteString,
                       signature: Array[Byte],
                       proposalTransactionID: String): Array[Byte] = {
    val commitHandler: CommitHandler = connection.gateway.getCommitHandlerFactory().create(ctx.getTxID(), connection.gateway.getNetwork(channel))
    commitHandler.startListening()
    try {
      // TODO not sure if we can get it here like this
      val transactionOptions: Channel.TransactionOptions = Channel.TransactionOptions.createTransactionOptions()
        .nOfEvents(Channel.NOfEvents.createNoEvents())

      internalSendTransaction(channelObj, signature, transactionOptions, proposalTransactionID, transactionPayloadBytes)
        .get(60, TimeUnit.SECONDS)
    } catch {
      case e: TimeoutException => commitHandler.cancelListening()
        throw e
      case e: Exception => commitHandler.cancelListening()
        throw new ContractException("Failed to send transaction to the orderer", e);
    }
    val commitTimeout: TimePeriod = new TimePeriod(5, TimeUnit.MINUTES)
    commitHandler.waitForEvents(commitTimeout.getTime(), commitTimeout.getTimeUnit());

    try {
      // TODO return transaction response payload
      val transactionPayload: Payload = Payload.parseFrom(transactionPayloadBytes)
      val chaincodeActionPayload = getChaincodeActionPayloadFromTransactionPayload(transactionPayload)
      val proposalResponsePayload = ProposalResponsePayload.parseFrom(chaincodeActionPayload.getAction.getProposalResponsePayload)
      val chaincodeAction = ChaincodeAction.parseFrom(proposalResponsePayload.getExtension)
      chaincodeAction.getResponse.getPayload.toByteArray
    } catch {
      case e: InvalidArgumentException => throw new GatewayRuntimeException(e)
    }
  }
}
