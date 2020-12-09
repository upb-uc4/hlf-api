package de.upb.cs.uc4.hyperledger.tests

import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Base64

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataMatriculation, TestHelper, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.impl.identity.X509IdentityImpl
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.hyperledger.fabric.sdk.transaction.TransactionContext

class UnsignedTransactionTests extends TestBase {

  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())
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
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelper.toPemString(testUserIdentity.getCertificate)
        val proposalBytes = certificateConnection.getProposalAddCertificate(certificate, organisationId, enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
      }
    }

    "passing a signed transaction" should {
      "submit the proposal transaction to the proposal contract" in {
        // enroll admin
        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        // create user
        // get testUser certificate and private key
        val argEnrollmentId = "frontend-signing-tester"
        val testAffiliation = organisationId
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(argEnrollmentId, testAffiliation)
        val privateKey: PrivateKey = testUserIdentity.getPrivateKey
        val certificate: X509Certificate = testUserIdentity.getCertificate

        // prepare test data
        val testMatData = TestDataMatriculation.validMatriculationData1(argEnrollmentId)
        println("\n\n\n##########################\nMatriculationTestData:\n##########################\n\n" + testMatData)

        // get proposal
        val proposalBytes = matriculationConnection.getProposalAddMatriculationData(Identities.toPemString(certificate), testAffiliation, testMatData)
        println("\n\n\n##########################\nProposal Bytes:\n##########################\n\n" + Base64.getEncoder.encodeToString(proposalBytes))

        // sign proposal with testUser privateKey
        val crypto: CryptoPrimitives = TestHelper.getCryptoPrimitives()
        val signatureBytes = crypto.sign(privateKey, proposalBytes)
        val b64Sig = ByteString.copyFrom(Base64.getEncoder.encode(signatureBytes)).toStringUtf8
        println("\n\n\n##########################\nSignature:\n##########################\n\n" + b64Sig)

        // get transaction
        val transactionPayloadBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(proposalBytes, signatureBytes)
        println("\n\n\n##########################\nTransactionPayload:\n##########################\n\n" + Base64.getEncoder.encodeToString(transactionPayloadBytes))

        // sign transaction and submit transaction
        val transactionSignature: Array[Byte] = crypto.sign(privateKey, transactionPayloadBytes)
        val result = matriculationConnection.submitSignedTransaction(transactionPayloadBytes, transactionSignature)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + result)

        // get approvals
        val getResult = initializeApproval(username).getApprovals("UC4.Matriculation", "addMatriculationData", testMatData)
        println("\n\n\n##########################\nCompareResult:\n##########################\n\n" + getResult)

        // test approvals submitted by the right entities
        getResult should contain(argEnrollmentId)
        getResult should contain(username)
      }
      "submit the real transaction to the real contract" in {
        // enroll admin
        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        // create user
        // get testUser certificate and private key
        val argEnrollmentId = "frontend-signing-tester2"
        val testAffiliation = organisationId
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(argEnrollmentId, testAffiliation)
        val privateKey: PrivateKey = testUserIdentity.getPrivateKey
        val certificate: X509Certificate = testUserIdentity.getCertificate

        // prepare test data
        val testMatData = TestDataMatriculation.validMatriculationData1(argEnrollmentId)
        println("\n\n\n##########################\nMatriculationTestData:\n##########################\n\n" + testMatData)

        // get proposal
        val proposalBytes = matriculationConnection.getProposalAddMatriculationData(Identities.toPemString(certificate), testAffiliation, testMatData)
        println("\n\n\n##########################\nProposal Bytes:\n##########################\n\n" + Base64.getEncoder.encodeToString(proposalBytes))

        // sign proposal with testUser privateKey
        val crypto: CryptoPrimitives = TestHelper.getCryptoPrimitives()
        val signatureBytes = crypto.sign(privateKey, proposalBytes)
        val b64Sig = ByteString.copyFrom(Base64.getEncoder.encode(signatureBytes)).toStringUtf8
        println("\n\n\n##########################\nSignature:\n##########################\n\n" + b64Sig)

        // get transaction
        val transactionPayloadBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(proposalBytes, signatureBytes)
        println("\n\n\n##########################\nTransactionPayload:\n##########################\n\n" + Base64.getEncoder.encodeToString(transactionPayloadBytes))

        // sign transaction and submit transaction
        val transactionSignature: Array[Byte] = crypto.sign(privateKey, transactionPayloadBytes)
        val result = matriculationConnection.submitSignedTransaction(transactionPayloadBytes, transactionSignature)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + result)

        // get approvals
        val getResult = initializeApproval(username).getApprovals("UC4.Matriculation", "addMatriculationData", testMatData)
        println("\n\n\n##########################\nCompareResult:\n##########################\n\n" + getResult)

        // test approvals submitted by the right entities
        getResult should contain(argEnrollmentId)
        getResult should contain(username)

        // test info stored
        val storedMatData = matriculationConnection.getMatriculationData(argEnrollmentId)
        TestHelper.compareJson(testMatData, storedMatData)
      }
    }

    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val enrollmentId = "101"
        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val proposalBytes = certificateConnection.getProposalAddCertificate(wrongCertificate, organisationId, enrollmentId, wrongCertificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val signature = ByteString.copyFrom(Base64.getDecoder.decode("MEUCIQD92OsJsVVFqFfifMV14ROiL5Ni/RaOBkR0DqzetvPfkQIgcrgu9vxr5TuZY6lft5adCETaC3CSE8QA+bs9MheeLcI="))
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.getUnsignedTransaction(proposalBytes, signature.toByteArray))
        result.actionName should be("validatePeerResponses")
      }
    }

    "testing info" should {
      "not fail 1" in {
        val enrollmentId = "105"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelper.toPemString(testUserIdentity.getCertificate)
        val inputMatJSon = TestDataMatriculation.validMatriculationData3("500")
        val proposalBytes = matriculationConnection.getProposalAddMatriculationData(certificate, organisationId, inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataProposal:: $info")
      }
      "not fail 2" in {
        val enrollmentId = "106"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelper.toPemString(testUserIdentity.getCertificate)
        val inputMatJSon = TestDataMatriculation.validMatriculationData4("500")
        val proposalBytes = matriculationConnection.getProposalUpdateMatriculationData(certificate, organisationId, inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataProposal:: $info")
      }
      "not fail 3" in {
        val enrollmentId = "107"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelper.toPemString(testUserIdentity.getCertificate)
        val proposalBytes = matriculationConnection.getProposalAddEntriesToMatriculationData(
          certificate,
          organisationId,
          "500",
          TestDataMatriculation.validMatriculationEntry
        )
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataProposal:: $info")
      }
    }
  }
}
