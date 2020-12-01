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
        val argEnrollmentId = "101"
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

        // get testUser certificate and private key
        val testUserIdentity: X509IdentityImpl = wallet.get(argEnrollmentId).asInstanceOf[X509IdentityImpl]

        val privateKey: PrivateKey = testUserIdentity.getPrivateKey()
        val certificate: X509Certificate = testUserIdentity.getCertificate()

        val adminIdentity: X509IdentityImpl = wallet.get(this.username).asInstanceOf[X509IdentityImpl]

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
        println("\n\n\n##########################\nProposal Bytes:\n##########################\n\n" + Base64.getEncoder.encodeToString(proposalBytes))

        // sign proposal with testUser privateKey
        val signatureBytes = crypto.sign(privateKey, proposalBytes)
        /*val proposalSignatureString: String = scala.io.Source.fromFile("/tmp/api-test/proposal_sign.txt").mkString.trim
        val signatureBytes: Array[Byte] = Base64.getDecoder.decode(proposalSignatureString)*/

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
        val transactionPayloadBytes: Array[Byte] = transactionPayload.toByteArray
        println("\n\n\n##########################\nTransactionPayload:\n##########################\n\n" + Base64.getEncoder.encodeToString(transactionPayloadBytes))

        val transactionSignature: Array[Byte] = crypto.sign(privateKey, transactionPayloadBytes)
        /*val transactionSignatureString: String = scala.io.Source.fromFile("/tmp/api-test/transaction_sign.txt").mkString.trim
        val transactionSignature: Array[Byte]= Base64.getDecoder.decode(transactionSignatureString)*/

        val response: Array[Byte] = TransactionHelper.sendTransaction(validResponses, certificateConnection, channel, ctx, channelObj, transactionPayload.toByteString, transactionSignature, proposalTransactionID)

        val result = new String(response, StandardCharsets.UTF_8)

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
        val result = certificateConnection.getUnsignedTransaction(proposalBytes, signature.toByteArray)
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
        val result = certificateConnection.getUnsignedTransaction(proposalBytes, signature.toByteArray)
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
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.getUnsignedTransaction(proposalBytes, signature.toByteArray))
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
