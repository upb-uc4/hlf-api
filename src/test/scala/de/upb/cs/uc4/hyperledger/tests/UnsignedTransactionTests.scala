package de.upb.cs.uc4.hyperledger.tests

import java.io.File
import java.lang.String.format
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.util
import java.util.{ArrayList, Base64, Collection, Collections, HashSet, LinkedList, List}
import java.util.concurrent.{CompletableFuture, TimeUnit, TimeoutException}

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCertificateTrait, ConnectionMatriculationTrait}
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.exceptions.traits.{HyperledgerExceptionTrait, TransactionExceptionTrait}
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.TestDataMatriculation
import de.upb.cs.uc4.hyperledger.utilities.{EnrollmentManager, RegistrationManager, WalletManager}
import de.upb.cs.uc4.hyperledger.utilities.helper.{Logger, ReflectionHelper, TransactionHelper}
import org.hyperledger.fabric.gateway.impl.{ContractImpl, GatewayImpl, TimePeriod, TransactionImpl}
import org.hyperledger.fabric.gateway.impl.identity.{GatewayUser, X509IdentityImpl}
import org.hyperledger.fabric.gateway.spi.CommitHandler
import org.hyperledger.fabric.gateway.{ContractException, GatewayRuntimeException, Identities, Identity, Wallet, X509Identity}
import org.hyperledger.fabric.protos.common.Common.{Envelope, Payload, Status}
import org.hyperledger.fabric.protos.orderer.Ab.BroadcastResponse
import org.hyperledger.fabric.protos.peer.{Chaincode, ProposalResponsePackage}
import org.hyperledger.fabric.protos.peer.ProposalPackage.{Proposal, SignedProposal}
import org.hyperledger.fabric.sdk.Channel.NOfEvents
import org.hyperledger.fabric.sdk.User.userContextCheck
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException
import org.hyperledger.fabric.sdk.helper.Config
import org.hyperledger.fabric.sdk.identity.X509Enrollment
import org.hyperledger.fabric.sdk.{BlockEvent, ChaincodeID, Channel, HFClient, NetworkConfig, Orderer, Peer, ProposalResponse, SDKUtils, TransactionProposalRequest, User}
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.hyperledger.fabric.sdk.transaction.{ProposalBuilder, TransactionBuilder, TransactionContext}

import scala.io.Source

class UnsignedTransactionTests extends TestBase {

  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
  }

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned transaction" should {
      "return an unsigned transaction" in {
        val enrollmentId = "100"
        val certificate = "Whatever"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
      }
    }

    "passing a signed transaction" should {
      "submit the proposal transaction to the proposal contract, even if the signature was not created using the private key belonging to the connection" in {
        // TODO dont use admin here!
        val argEnrollmentId = "101"
        //val argEnrollmentId = this.username
        val argCertificate = "Whatever"
        val testAffiliation = "org1MSP"

        val wallet: Wallet = WalletManager.getWallet(this.walletPath)

        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)
        // try register and enroll test user 102
        try {
          val testUserPw = RegistrationManager.register(caURL, tlsCert, argEnrollmentId, username, walletPath, testAffiliation)
          EnrollmentManager.enroll(caURL, tlsCert, walletPath, argEnrollmentId, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)
        } catch {
          case _: Throwable =>
        }

        // initialize crypto primitives
        val crypto: CryptoPrimitives = new CryptoPrimitives()
        val securityLevel: Integer = 256
        ReflectionHelper.safeCallPrivateMethod(crypto)("setSecurityLevel")(securityLevel)
        // TODO use get- and set properties
        //val mspId: String = ""
        //val certificatePem: String = ""

        // get testUser certificate and private key
        val testUserIdentity: X509IdentityImpl = wallet.get(argEnrollmentId).asInstanceOf[X509IdentityImpl]
        // val privateKeyPem: String = ""
        //val certificatePem: String = ""
        // val privateKey: PrivateKey = Identities.readPrivateKey(privateKeyPem)
        // val certificate: X509Certificate = Identities.readX509Certificate(certificatePem)
        //val identity: X509Identity = Identities.newX509Identity(mspId, certificate, privateKey)

        val privateKey: PrivateKey = testUserIdentity.getPrivateKey()
        val certificate: X509Certificate = testUserIdentity.getCertificate()
        // TODO proper values here?

        // mock certificate (replace admin mspId by testUser mspId)
        val adminIdentity: X509IdentityImpl = wallet.get(this.username).asInstanceOf[X509IdentityImpl]
        // val originalCertificate: X509Certificate = adminIdentity.getCertificate()
        // ReflectionHelper.setPrivateField(adminIdentity)("certificate")(certificate)
        // val originalMspId: String = adminIdentity.getMspId()
        // ReflectionHelper.setPrivateField(adminIdentity)("mspId")(testUserIdentity.getMspId())
        // wallet.remove(this.username)
        // wallet.put(this.username, adminIdentity)

        // get proposal
        //val proposalBytes = certificateConnection.getProposalAddCertificate(argEnrollmentId, argCertificate)
        val enrollment: X509Enrollment = new X509Enrollment(new PrivateKey {
          override def getAlgorithm: String = null
          override def getFormat: String = null
          override def getEncoded: Array[Byte] = null
        }, Identities.toPemString(certificate))
        val user: User = new GatewayUser(argEnrollmentId, testAffiliation, enrollment);
        // val user: User = new GatewayUser(argEnrollmentId, testAffiliation, new X509Enrollment(adminIdentity.getPrivateKey, Identities.toPemString(adminIdentity.getCertificate)))
        val request = TransactionProposalRequest.newInstance(user)
        request.setChaincodeName(this.chaincode)
        request.setFcn("UC4.Approval:approveTransaction")
        request.setArgs("UC4.Certificate" ,"addCertificate", "[\"" + argEnrollmentId + "\",\"" + argCertificate + "\"]")
        val networkConfigFile: File = networkDescriptionPath.toFile()
        val networkConfig: NetworkConfig = NetworkConfig.fromYamlFile(networkConfigFile)
        val hfClient: HFClient = HFClient.createNewInstance()
        hfClient.setCryptoSuite(crypto)
        hfClient.setUserContext(user)
        // val channelObj: Channel = hfClient.loadChannelFromConfig(channel, networkConfig)
        val channelObj: Channel = certificateConnection.gateway.getNetwork(channel).getChannel
        val ctx: TransactionContext = new TransactionContext(channelObj, user, crypto)
        val chaincodeId: Chaincode.ChaincodeID = Chaincode.ChaincodeID.newBuilder().setName(this.chaincode).build()
        val proposal = ProposalBuilder.newBuilder().context(ctx).request(request).chaincodeID(chaincodeId).build()
        //val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nPROPOSALBYTES:\n##########################\n\n" + proposal.toByteString.toStringUtf8)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)

        val proposalBytes: Array[Byte] = proposal.toByteArray
        // sign proposal with testUser privateKey
        val signatureBytes = crypto.sign(privateKey, proposalBytes)
        // val signatureBytes = crypto.sign(adminIdentity.getPrivateKey, proposalBytes)

        val b64Sig = ByteString.copyFrom(Base64.getEncoder.encode(signatureBytes)).toStringUtf8
        println("\n\n\n##########################\nSignature:\n##########################\n\n" + b64Sig)

        // submit only signed transaction to approval contract
        val signature: ByteString = ByteString.copyFrom(signatureBytes)
        // create signedProposal Object and get Info Objects
        val (transaction: TransactionImpl, context: TransactionContext, signedProposal: SignedProposal) =
          TransactionHelper.createSignedProposal(certificateConnection.approvalConnection.get, proposal, signature)

        // get rid of connection with identity by:
        //val gateway: GatewayImpl = new GatewayImpl.Builder().networkConfig(networkDescriptionPath).connect()
        //val contract: ContractImpl = new ContractImpl(network, chaincodeId, "UC4.Approval")


        // submit approval
        // propose transaction
        // TODO comment out next line
        // val result = certificateConnection.internalSubmitApprovalProposal(transaction, ctx, signedProposal)
        // get rid of connection with identity by:
        val peers: util.Collection[Peer] = ReflectionHelper.safeCallPrivateMethod(channelObj)("getEndorsingPeers")().asInstanceOf[util.Collection[Peer]]
        val proposalResponses = ReflectionHelper.safeCallPrivateMethod(channelObj)("sendProposalToPeers")(peers, signedProposal, ctx).asInstanceOf[util.Collection[ProposalResponse]]
        // commit transaction
        val validResponses = ReflectionHelper.safeCallPrivateMethod(transaction)("validatePeerResponses")(proposalResponses).asInstanceOf[util.Collection[ProposalResponse]]
        // dissect val result = ReflectionHelper.safeCallPrivateMethod(transaction)("commitTransaction")(validResponses).asInstanceOf[Array[Byte]]
        val (transactionPayload, proposalTransactionID) = TransactionHelper.getTransaction(validResponses, channelObj)
        val transactionSignature: Array[Byte] = crypto.sign(privateKey, transactionPayload.toByteString.toByteArray)
        val response: Array[Byte] = TransactionHelper.sendTransaction(validResponses, certificateConnection, channel, ctx, channelObj, transactionPayload, transactionSignature, proposalTransactionID)
        /*val response: Array[Byte] = {
          val proposalResponse: ProposalResponse = validResponses.iterator().next()
          val commitHandler: CommitHandler = certificateConnection.gateway.getCommitHandlerFactory().create(ctx.getTxID(), certificateConnection.gateway.getNetwork(channel))
          commitHandler.startListening()
          def sendTransaction(channel: Channel, validResponses: util.Collection[ProposalResponse], transactionOptions: Channel.TransactionOptions): CompletableFuture[BlockEvent#TransactionEvent] = {
            try {
              if (null == transactionOptions) throw new InvalidArgumentException("Parameter transactionOptions can't be null")
              ReflectionHelper.safeCallPrivateMethod(channel)("checkChannelState")()
              if (null == proposalResponses) throw new InvalidArgumentException("sendTransaction proposalResponses was null")
              val orderers = if (ReflectionHelper.getPrivateField(transactionOptions)("orderers")() != null) ReflectionHelper.getPrivateField(transactionOptions)("orderers")().asInstanceOf[util.List[Orderer]]
              else new util.ArrayList[Orderer](channelObj.getOrderers())
              // make certain we have our own copy
              val shuffeledOrderers: util.ArrayList[Orderer] = new util.ArrayList[Orderer](orderers)
              if (ReflectionHelper.getPrivateField(transactionOptions)("shuffleOrders")().asInstanceOf[Boolean]) Collections.shuffle(shuffeledOrderers)
              if (ReflectionHelper.getPrivateField(channelObj)("config")().asInstanceOf[Config].getProposalConsistencyValidation) {
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
              proposalResponses.forEach (
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
              val transactionPayload = transactionBuilder.chaincodeProposal(proposal).endorsements(ed).proposalResponsePayload(proposalResponsePayload).build
              def createTransactionEnvelope(transactionPayload: Payload, transactionContext: TransactionContext): Envelope = {
                Envelope.newBuilder.setPayload(transactionPayload.toByteString).setSignature(ByteString.copyFrom(crypto.sign(privateKey, transactionPayload.toByteString.toByteArray))).build
              }
              val transactionEnvelope = createTransactionEnvelope(transactionPayload, transactionContext)
              var nOfEvents = ReflectionHelper.getPrivateField(transactionOptions)("nOfEvents")().asInstanceOf[NOfEvents]
              if (nOfEvents == null) {
                nOfEvents = NOfEvents.createNofEvents
                val eventingPeers = ReflectionHelper.safeCallPrivateMethod(channelObj)("getEventingPeers")().asInstanceOf[util.Collection[Peer]]
                var anyAdded = false
                if (!eventingPeers.isEmpty) {
                  anyAdded = true
                  nOfEvents.addPeers(eventingPeers)
                }
                if (!anyAdded) nOfEvents = NOfEvents.createNoEvents
              }
              else if (nOfEvents ne NOfEvents.nofNoEvents) {
                val issues = new StringBuilder(100)
                val eventingPeers = ReflectionHelper.safeCallPrivateMethod(channelObj)("getEventingPeers")().asInstanceOf[util.Collection[Peer]]
                ReflectionHelper.safeCallPrivateMethod(nOfEvents)("unSeenPeers")().asInstanceOf[util.Collection[Peer]].forEach((peer: Peer) => {
                  def foo(peer: Peer) = if (ReflectionHelper.safeCallPrivateMethod(peer)("getChannel")() ne this) issues.append(format("Peer %s added to NOFEvents does not belong this channel. ", peer.getName))
                  else if (!eventingPeers.contains(peer)) issues.append(format("Peer %s added to NOFEvents is not a eventing Peer in this channel. ", peer.getName))

                  foo(peer)
                })
                if (ReflectionHelper.safeCallPrivateMethod(nOfEvents)("unSeenPeers")().asInstanceOf[util.Collection[Peer]].isEmpty) issues.append("NofEvents had no added  Peer eventing services.")
                val foundIssues = issues.toString
                if (!foundIssues.isEmpty) throw new InvalidArgumentException(foundIssues)
              }
              val replyonly = (nOfEvents eq NOfEvents.nofNoEvents) || ReflectionHelper.safeCallPrivateMethod(channelObj)("getEventingPeers")().asInstanceOf[util.Collection[Peer]].isEmpty
              var sret: CompletableFuture[BlockEvent#TransactionEvent] = null
              if (replyonly) { //If there are no eventsto complete the future, complete it
                // immediately but give no transaction event
                //logger.debug(format("Completing transaction id %s immediately no peer eventing services found in channel %s.", proposalTransactionID, name))
                sret = new CompletableFuture[BlockEvent#TransactionEvent]
              }
              else sret = ReflectionHelper.safeCallPrivateMethod(channelObj)("registerTxListener")(proposalTransactionID, nOfEvents, ReflectionHelper.getPrivateField(transactionOptions)("failFast")()).asInstanceOf[CompletableFuture[BlockEvent#TransactionEvent]]
              //logger.debug(format("Channel %s sending transaction to orderer(s) with TxID %s ", name, proposalTransactionID))
              var success: Boolean = false
              var lException: Exception = null // Save last exception to report to user .. others are just logged.
              var resp: BroadcastResponse = null
              var failed: Orderer = null
              //import scala.collection.JavaConversions._
              shuffeledOrderers.forEach((orderer: Orderer) =>  {
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
                      var emsg = format("Channel %s unsuccessful sendTransaction to orderer %s (%s)", channelObj.getName(), orderer.getName, orderer.getUrl)
                      if (resp != null) emsg = format("Channel %s unsuccessful sendTransaction to orderer %s (%s).  %s", channelObj.getName, orderer.getName, orderer.getUrl, ReflectionHelper.safeCallPrivateMethod(channelObj)("getRespData")(resp).asInstanceOf[String])
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
                val emsg = format("Channel %s failed to place transaction %s on Orderer. Cause: UNSUCCESSFUL. %s", channelObj.getName, proposalTransactionID, ReflectionHelper.safeCallPrivateMethod(channelObj)("getRespData")(resp).asInstanceOf[String])
                ReflectionHelper.safeCallPrivateMethod(channelObj)("unregisterTxListener")(proposalTransactionID)
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
          try {
            val transactionOptions: Channel.TransactionOptions = Channel.TransactionOptions.createTransactionOptions()
              .nOfEvents(Channel.NOfEvents.createNoEvents()); // Disable default commit wait behaviour
            sendTransaction(channelObj, validResponses, transactionOptions)
              .get(60, TimeUnit.SECONDS);
          } catch {
            case e: TimeoutException => commitHandler.cancelListening()
              throw e
            case e: Exception => commitHandler.cancelListening()
              throw new ContractException("Failed to send transaction to the orderer", e);
          }
          val commitTimeout: TimePeriod = new TimePeriod(5, TimeUnit.MINUTES)
          commitHandler.waitForEvents(commitTimeout.getTime(), commitTimeout.getTimeUnit());

          try {
            proposalResponse.getChaincodeActionResponsePayload();
          } catch {
            case e: InvalidArgumentException => throw new GatewayRuntimeException(e)
          }
        }*/
        val result = new String(response, StandardCharsets.UTF_8)
        // reset certificate of admin user
        // ReflectionHelper.setPrivateField(adminIdentity)("certificate")(originalCertificate)
        // ReflectionHelper.setPrivateField(adminIdentity)("mspId")(originalMspId)
        // wallet.remove(this.username)
        // wallet.put(this.username, adminIdentity)

        println("\n\n\n##########################\nResult:\n##########################\n\n" + result)
      }
      "submit the proposal transaction to the proposal contract" in {
        val enrollmentId = "102"
        val certificate = "Whatever"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nPROPOSALBYTES:\n##########################\n\n" + proposal.toByteString.toStringUtf8)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val transactionContext: TransactionContext = certificateConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        val b64Sig = ByteString.copyFrom(Base64.getEncoder.encode(signature.toByteArray)).toStringUtf8
        println("\n\n\n##########################\nSignature:\n##########################\n\n" + b64Sig)
        val result = certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + result)
      }
      "submit the real transaction to the real contract" in {
        // store info
        val enrollmentId = "103"
        val certificate = "Whatever"
        println("\n\n\n##########################\nGET PROPOSAL:\n##########################\n\n")
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val transactionContext: TransactionContext = certificateConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        println("\n\n\n##########################\nSUBMIT PROPOSAL:\n##########################\n\n")
        val result = certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray)
        println("\n\n\n##########################\nResult103:\n##########################\n\n" + result)

        // test info stored
        val storedCert = certificateConnection.getCertificate(enrollmentId)
        storedCert should be(certificate)
      }
    }

    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val approvalTransactionName = "approveTransaction"
        val enrollmentId = "101"
        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, wrongCertificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val signature = ByteString.copyFrom(Base64.getDecoder.decode("MEUCIQD92OsJsVVFqFfifMV14ROiL5Ni/RaOBkR0DqzetvPfkQIgcrgu9vxr5TuZY6lft5adCETaC3CSE8QA+bs9MheeLcI="))
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray))
        result.actionName should be(approvalTransactionName)
      }
    }

    "testing info" should {
      "not fail 1" in {
        val inputMatJSon = TestDataMatriculation.validMatriculationData3("500")
        val proposalBytes = matriculationConnection.getProposalAddMatriculationData(inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataProposal:: $info")
      }
      "not fail 2" in {
        val inputMatJSon = TestDataMatriculation.validMatriculationData4("500")
        val proposalBytes = matriculationConnection.getProposalUpdateMatriculationData(inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataProposal:: $info")
      }
      "not fail 3" in {
        val proposalBytes = matriculationConnection.getProposalAddEntriesToMatriculationData(
          "500",
          TestDataMatriculation.validMatriculationEntry
        )
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataProposal:: $info")
      }
    }
  }
}
