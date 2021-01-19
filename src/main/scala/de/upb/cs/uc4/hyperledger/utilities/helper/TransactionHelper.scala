package de.upb.cs.uc4.hyperledger.utilities.helper

import java.io.File
import java.lang.String.format
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.PrivateKey
import java.util
import java.util.Collections
import java.util.concurrent.{ CompletableFuture, TimeUnit, TimeoutException }

import com.google.gson.Gson
import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionOperationTrait, ConnectionTrait }
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, TimePeriod, TransactionImpl }
import org.hyperledger.fabric.gateway.spi.CommitHandler
import org.hyperledger.fabric.gateway.{ ContractException, GatewayRuntimeException }
import org.hyperledger.fabric.protos.common.Common
import org.hyperledger.fabric.protos.common.Common.{ Envelope, Payload, Status }
import org.hyperledger.fabric.protos.orderer.Ab.BroadcastResponse
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ ChaincodeAction, ChaincodeProposalPayload, Proposal, SignedProposal }
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage.ProposalResponsePayload
import org.hyperledger.fabric.protos.peer.TransactionPackage.{ ChaincodeActionPayload, Transaction }
import org.hyperledger.fabric.protos.peer.{ Chaincode, ProposalPackage, ProposalResponsePackage }
import org.hyperledger.fabric.sdk.Channel.NOfEvents
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException
import org.hyperledger.fabric.sdk.helper.Config
import org.hyperledger.fabric.sdk.identity.X509Enrollment
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.hyperledger.fabric.sdk.transaction.{ ProposalBuilder, TransactionBuilder, TransactionContext }
import org.hyperledger.fabric.sdk._

import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters._
import scala.util.control.Breaks.{ break, breakable }

protected[hyperledger] object TransactionHelper {

  def getParametersFromTransactionPayload(payload: Payload): (String, Seq[String]) = {
    val chaincodeTransactionPayload: ChaincodeActionPayload = getChaincodeActionPayloadFromTransactionPayload(payload)
    val chaincodeProposalPayload: ChaincodeProposalPayload = ChaincodeProposalPayload.parseFrom(chaincodeTransactionPayload.getChaincodeProposalPayload)
    val args: Seq[String] = getArgsFromChaincodeProposalPayload(chaincodeProposalPayload)
    (args.head, args.tail)
  }

  def getChaincodeActionPayloadFromTransactionPayload(payload: Payload): ChaincodeActionPayload = {
    val transaction = Transaction.parseFrom(payload.getData)
    ChaincodeActionPayload.parseFrom(transaction.getActions(0).getPayload)
  }

  private def getArgsFromChaincodeProposalPayload(payload: ChaincodeProposalPayload): Seq[String] = {
    val invocationSpec: Chaincode.ChaincodeInvocationSpec = Chaincode.ChaincodeInvocationSpec.parseFrom(payload.getInput)
    val chaincodeInput = invocationSpec.getChaincodeSpec.getInput
    val args: Array[ByteString] = chaincodeInput.getArgsList.asScala.toArray
    args.map[String]((b: ByteString) => new String(b.toByteArray, StandardCharsets.UTF_8)).toList
  }

  def getApprovalParameterList(initiator: String, contractName: String, transactionName: String, params: Array[String]): Seq[String] = {
    val jsonParams = new Gson().toJson(params)
    List[String](initiator, contractName, transactionName, jsonParams)
  }

  def createTransactionInfo(contract: ContractImpl, transactionName: String, params: Seq[String], transactionId: Option[String]): (TransactionImpl, TransactionContext, TransactionProposalRequest) = {
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = ReflectionHelper.safeCallPrivateMethod(transaction)("newProposalRequest")(params.toArray).asInstanceOf[TransactionProposalRequest]
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

  def createSignedProposal(operationConnection: ConnectionOperationTrait, proposal: ProposalPackage.Proposal, signature: ByteString): (TransactionImpl, SignedProposal) = {
    val transactionId: String = TransactionHelper.getTransactionIdFromProposal(proposal)
    val transactionName: String = TransactionHelper.getTransactionNameFromProposal(proposal)
    val params: Seq[String] = TransactionHelper.getTransactionParamsFromProposal(proposal)

    val signedProposalBuilder: SignedProposal.Builder = SignedProposal.newBuilder
      .setProposalBytes(proposal.toByteString)
      .setSignature(signature)
    val signedProposal: SignedProposal = signedProposalBuilder.build

    val (transaction, _, _) = TransactionHelper.createTransactionInfo(operationConnection.contract, transactionName, params, Some(transactionId))

    (transaction, signedProposal)
  }

  /** @param certificate            the certificate of the user to create the proposal for
    * @param userAffiliation        the affiliation of the user to create the proposal for
    * @param chaincodeName          the chaincodeName to create a proposal for
    * @param channelName            the channelName to create a proposal for
    * @param function               the hyperledger fcnName (contractName:transactionName) describing the proposal to be created
    * @param networkDescriptionPath path to the network description file containing information about peers and orderers
    * @param args                   parameters given to the fcn for invocation in the proposal
    * @return The proposalObject created
    */
  def createProposal(
      certificate: String,
      userAffiliation: String,
      chaincodeName: String,
      channelName: String,
      function: String,
      networkDescriptionPath: Path,
      args: String*
  ): Array[Byte] = {
    val enrollment: X509Enrollment = new X509Enrollment(new PrivateKey {
      override def getAlgorithm: String = null

      override def getFormat: String = null

      override def getEncoded: Array[Byte] = null
    }, certificate)
    val user: User = new GatewayUser("gateway", userAffiliation, enrollment)
    val request = TransactionProposalRequest.newInstance(user)
    request.setChaincodeName(chaincodeName)
    request.setFcn(function)
    request.setArgs(args: _*)
    val networkConfigFile: File = networkDescriptionPath.toFile
    val networkConfig: NetworkConfig = NetworkConfig.fromYamlFile(networkConfigFile)
    val hfClient: HFClient = HFClient.createNewInstance()
    val crypto: CryptoPrimitives = new CryptoPrimitives()
    val securityLevel: Integer = 256
    ReflectionHelper.safeCallPrivateMethod(crypto)("setSecurityLevel")(securityLevel)
    hfClient.setCryptoSuite(crypto)
    hfClient.setUserContext(user)
    val channelObj: Channel = hfClient.loadChannelFromConfig(channelName, networkConfig)
    val ctx: TransactionContext = new TransactionContext(channelObj, user, crypto)
    val chaincodeId: Chaincode.ChaincodeID = Chaincode.ChaincodeID.newBuilder().setName(chaincodeName).build()
    val proposal: Proposal = ProposalBuilder.newBuilder().context(ctx).request(request).chaincodeID(chaincodeId).build()
    proposal.toByteArray
  }

  def getTransaction(
      validResponses: util.Collection[ProposalResponse],
      channelObj: Channel
  ): Payload = {
    // Disable default commit wait behaviour
    val transactionOptions: Channel.TransactionOptions =
      Channel.TransactionOptions.createTransactionOptions().nOfEvents(Channel.NOfEvents.createNoEvents())
    if (null == transactionOptions) throw new InvalidArgumentException("Parameter transactionOptions can't be null")

    ReflectionHelper.safeCallPrivateMethod(channelObj)("checkChannelState")()
    if (null == validResponses) throw new InvalidArgumentException("sendTransaction proposalResponses was null")

    // make certain we have our own copy
    if (ReflectionHelper.getPrivateField(channelObj)("config").asInstanceOf[Config].getProposalConsistencyValidation) {
      val invalid = new util.HashSet[ProposalResponse]
      val consistencyGroups = SDKUtils.getProposalConsistencySets(validResponses, invalid).size
      if (consistencyGroups != 1 || !invalid.isEmpty) throw new IllegalArgumentException(format("The proposal responses have %d inconsistent groups with %d that are invalid." + " Expected all to be consistent and none to be invalid.", consistencyGroups, invalid.size))
    }

    // gather information
    val ed = new util.LinkedList[ProposalResponsePackage.Endorsement]
    var proposal: Proposal = null
    var proposalResponsePayload: ByteString = null
    var proposalTransactionID: String = null
    var transactionContext: TransactionContext = null
    validResponses.forEach(
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

    // build transaction
    TransactionBuilder.newBuilder
      .chaincodeProposal(proposal)
      .endorsements(ed)
      .proposalResponsePayload(proposalResponsePayload)
      .build
  }

  def sendTransaction(
      connection: ConnectionTrait,
      channel: String,
      ctx: TransactionContext,
      channelObj: Channel,
      transactionPayloadBytes: ByteString,
      signature: Array[Byte],
      proposalTransactionID: String
  ): Array[Byte] = {
    val commitHandler: CommitHandler = connection.gateway.getCommitHandlerFactory.create(ctx.getTxID, connection.gateway.getNetwork(channel))
    commitHandler.startListening()
    try {
      val transactionOptions: Channel.TransactionOptions = Channel.TransactionOptions.createTransactionOptions()
        .nOfEvents(Channel.NOfEvents.createNoEvents())

      internalSendTransaction(channelObj, signature, transactionOptions, proposalTransactionID, transactionPayloadBytes)
        .get(60, TimeUnit.SECONDS)
    }
    catch {
      case e: TimeoutException =>
        commitHandler.cancelListening()
        throw e
      case e: Exception =>
        commitHandler.cancelListening()
        throw new ContractException("Failed to send transaction to the orderer", e)
    }
    val commitTimeout: TimePeriod = new TimePeriod(5, TimeUnit.MINUTES)
    commitHandler.waitForEvents(commitTimeout.getTime, commitTimeout.getTimeUnit)

    try {
      val transactionPayload: Payload = Payload.parseFrom(transactionPayloadBytes)
      val chaincodeActionPayload = getChaincodeActionPayloadFromTransactionPayload(transactionPayload)
      val proposalResponsePayload = ProposalResponsePayload.parseFrom(chaincodeActionPayload.getAction.getProposalResponsePayload)
      val chaincodeAction = ChaincodeAction.parseFrom(proposalResponsePayload.getExtension)
      chaincodeAction.getResponse.getPayload.toByteArray
    }
    catch {
      case e: InvalidArgumentException => throw new GatewayRuntimeException(e)
    }
  }

  def internalSendTransaction(
      channel: Channel,
      signature: Array[Byte],
      transactionOptions: Channel.TransactionOptions,
      proposalTransactionID: String,
      transactionPayload: ByteString
  ): CompletableFuture[BlockEvent#TransactionEvent] = {
    try {
      val orderers = if (ReflectionHelper.getPrivateField(transactionOptions)("orderers") != null) ReflectionHelper.getPrivateField(transactionOptions)("orderers").asInstanceOf[util.List[Orderer]]
      else new util.ArrayList[Orderer](channel.getOrderers)
      val shuffledOrderers: util.ArrayList[Orderer] = new util.ArrayList[Orderer](orderers)
      if (ReflectionHelper.getPrivateField(transactionOptions)("shuffleOrders").asInstanceOf[Boolean]) Collections.shuffle(shuffledOrderers)
      val transactionEnvelope = setTransactionSignature(transactionPayload, signature)
      var nOfEvents = ReflectionHelper.getPrivateField(transactionOptions)("nOfEvents").asInstanceOf[NOfEvents]
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
      val replyOnly = (nOfEvents eq NOfEvents.nofNoEvents) || ReflectionHelper.safeCallPrivateMethod(channel)("getEventingPeers")().asInstanceOf[util.Collection[Peer]].isEmpty
      var sret: CompletableFuture[BlockEvent#TransactionEvent] = null
      if (replyOnly) { //If there are no events to complete the future, complete it
        // immediately but give no transaction event
        //logger.debug(format("Completing transaction id %s immediately no peer eventing services found in channel %s.", proposalTransactionID, name))
        sret = new CompletableFuture[BlockEvent#TransactionEvent]
      }
      else sret = ReflectionHelper.safeCallPrivateMethod(channel)("registerTxListener")(proposalTransactionID, nOfEvents, ReflectionHelper.getPrivateField(transactionOptions)("failFast")).asInstanceOf[CompletableFuture[BlockEvent#TransactionEvent]]
      var success: Boolean = false
      var lException: Exception = null // Save last exception to report to user .. others are just logged.
      var resp: BroadcastResponse = null
      var failed: Orderer = null
      breakable {
        shuffledOrderers.forEach((orderer: Orderer) => {
          failed = orderer
          try {
            resp = ReflectionHelper.safeCallPrivateMethod(orderer)("sendTransaction")(transactionEnvelope).asInstanceOf[BroadcastResponse]
            lException = null // no longer last exception .. maybe just failed.

            if (resp.getStatus eq Status.SUCCESS) {
              success = true
              break
            }
          }
          catch {
            case e: Exception =>
              var exceptionMessage = format("Channel %s unsuccessful sendTransaction to orderer %s (%s)", channel.getName, orderer.getName, orderer.getUrl)
              if (resp != null) exceptionMessage = format("Channel %s unsuccessful sendTransaction to orderer %s (%s).  %s", channel.getName, orderer.getName, orderer.getUrl, ReflectionHelper.safeCallPrivateMethod(channel)("getRespData")(resp).asInstanceOf[String])
              lException = new Exception(exceptionMessage, e)
          }
        })
      }
      if (success) {
        if (replyOnly) sret.complete(null) // just say we're done.
        sret
      }
      else {
        val exceptionMessage = format("Channel %s failed to place transaction %s on Orderer. Cause: UNSUCCESSFUL. %s", channel.getName, proposalTransactionID, ReflectionHelper.safeCallPrivateMethod(channel)("getRespData")(resp).asInstanceOf[String])
        ReflectionHelper.safeCallPrivateMethod(channel)("unregisterTxListener")(proposalTransactionID)
        val ret = new CompletableFuture[BlockEvent#TransactionEvent]
        ret.completeExceptionally(if (lException != null) new Exception(exceptionMessage, lException)
        else new Exception(exceptionMessage))
        ret
      }
    }
    catch {
      case e: Exception =>
        val future = new CompletableFuture[BlockEvent#TransactionEvent]
        future.completeExceptionally(e)
        future
    }
  }

  def setTransactionSignature(transactionPayload: ByteString, signature: Array[Byte]): Envelope = {
    Envelope.newBuilder.setPayload(transactionPayload).setSignature(ByteString.copyFrom(signature)).build
  }

  private def getArgsFromProposal(proposal: Proposal): Seq[String] = {
    val payloadBytes: ByteString = proposal.getPayload
    val payload: ChaincodeProposalPayload = ProposalPackage.ChaincodeProposalPayload.parseFrom(payloadBytes)
    getArgsFromChaincodeProposalPayload(payload)
  }

  def getTransactionInfoFromOperation(operationInfo: String): String = {
    Logger.debug("OPERATIONINFO " + operationInfo)
    val transactionInfo = operationInfo
      .replace(" ", "")
      .replace("\n", "")
      .split(""""transactionInfo":\{""").tail.head // index 1
      .split("""},"initiator""").head
    Logger.debug("TRANSACTIONINFO 1 " + transactionInfo)
    transactionInfo
  }

  def getInfoFromTransactionInfo(transactionInfo: String): (String, String, Seq[String]) = {
    Logger.debug("TRANSACTIONINFO 2 " + transactionInfo)
    val contractName: String = transactionInfo
      .split("contractName\":\"").tail.head
      .split("\"").head
    val transactionName: String = transactionInfo
      .split("transactionName\":\"").tail.head
      .split("\"").head
    val transactionParamsStringPlus1: String = transactionInfo
      .split("parameters\":\"").tail.head
    val transactionParamsString = transactionParamsStringPlus1.substring(0, transactionParamsStringPlus1.lastIndexOf("\""))

    Logger.debug("TRANSACTIONPARAMS STRING " + transactionParamsString)
    val transactionPString: String = "[\"EnrollmentID_001\",\"legit_certificate\"]"

    val transactionParamsArrayList: util.ArrayList[String] = new Gson().fromJson(transactionParamsString, classOf[util.ArrayList[String]])
    val transactionParams: Seq[String] = CollectionConverters.IterableHasAsScala(transactionParamsArrayList).asScala.toSeq
    (contractName, transactionName, transactionParams)
  }
}
