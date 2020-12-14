package de.upb.cs.uc4.hyperledger.tests

import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Base64

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataMatriculation, TestHelper, TestHelperCrypto, TestHelperStrings, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.impl.identity.X509IdentityImpl
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk.security.CryptoPrimitives

class UnsignedTransactionTests extends TestBase {

  val crypto: CryptoPrimitives = TestHelperCrypto.getCryptoPrimitives
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
    "querying for an unsigned proposal" should {
      "return an unsigned proposal" in {
        val enrollmentId = "100"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)
        val (approvalResult, proposalBytes) = certificateConnection.getProposalUpdateCertificate(certificate, organisationId, enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        val header = proposal.getHeader.toStringUtf8

        // header contains signing user certificate
        header should include(certificate)

        // payload contains Approval TransactionInfo
        TestHelper.testProposalPayloadBytesContainsInfo(proposalBytes, Seq(
          "UC4.Certificate",
          "updateCertificate",
          enrollmentId,
          certificate
        ))
      }
    }
    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val enrollmentId = "101"
        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val (approvalResult, proposalBytes) = certificateConnection.getProposalUpdateCertificate(wrongCertificate, organisationId, enrollmentId, wrongCertificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)

        // fake signature
        val signature = ByteString.copyFrom(Base64.getDecoder.decode("MEUCIQD92OsJsVVFqFfifMV14ROiL5Ni/RaOBkR0DqzetvPfkQIgcrgu9vxr5TuZY6lft5adCETaC3CSE8QA+bs9MheeLcI="))
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.getUnsignedTransaction(proposalBytes, signature.toByteArray))
        result.actionName should be("validatePeerResponses")
      }
    }
    "getting a proposal for a faulty transaction " should {
      "deny getting the proposal and throw a matching exception" in {
        val enrollmentId = "102"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)
        val exception = intercept[TransactionExceptionTrait](certificateConnection.getProposalAddCertificate(certificate, organisationId, enrollmentId, certificate))
        exception.transactionName should be("addCertificate")
        exception.payload should include("HLConflict")
      }
    }
  }

  "The ConnectionMatriculation" when {
    "passing a signed transaction" should {
      "submit the approval transaction to the approval contract" in {
        // create user
        // get testUser certificate and private key
        val argEnrollmentId = "frontend-signing-tester"
        val testAffiliation = organisationId
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(argEnrollmentId, testAffiliation)
        val privateKey: PrivateKey = testUserIdentity.getPrivateKey
        val certificate: X509Certificate = testUserIdentity.getCertificate

        // prepare test data
        val testMatData = TestDataMatriculation.validMatriculationData1(argEnrollmentId)

        // get proposal
        val (proposalApprovalResult, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(
          Identities.toPemString(certificate),
          jSonMatriculationData = testMatData
        )
        TestHelper.testProposalPayloadBytesContainsInfo(
          proposalBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )
        proposalApprovalResult should include(username)

        // get transaction for signature
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(
          proposalBytes,
          crypto.sign(privateKey, proposalBytes)
        )
        TestHelper.testTransactionBytesContainsInfo(
          transactionBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )

        // sign transaction and submit transaction
        val (transactionApprovalResult, transactionResult) = matriculationConnection.submitSignedTransaction(
          transactionBytes, crypto.sign(privateKey, transactionBytes)
        )
        transactionApprovalResult should include(argEnrollmentId)
        transactionApprovalResult should include(username)
        TestHelperStrings.compareJson(testMatData, transactionResult)

        // test approvals on ledger
        val getResult = initializeApproval(username).getApprovals("UC4.MatriculationData", "addMatriculationData", testMatData)
        getResult should include(argEnrollmentId)
        getResult should include(username)
      }
      "submit the real transaction to the real contract" in {
        // create user
        // get testUser certificate and private key
        val argEnrollmentId = "frontend-signing-tester2"
        val testAffiliation = organisationId
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(argEnrollmentId, testAffiliation)
        val privateKey: PrivateKey = testUserIdentity.getPrivateKey
        val certificate: X509Certificate = testUserIdentity.getCertificate

        // prepare test data
        val testMatData = TestDataMatriculation.validMatriculationData1(argEnrollmentId)

        // get proposal
        val (_, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(Identities.toPemString(certificate), testAffiliation, testMatData)
        TestHelper.testProposalPayloadBytesContainsInfo(
          proposalBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )

        // get transaction for signature
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(
          proposalBytes,
          crypto.sign(privateKey, proposalBytes)
        )
        TestHelper.testTransactionBytesContainsInfo(
          transactionBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )

        // sign transaction and submit transaction
        val (transactionApprovalResult, realTransactionResult) = matriculationConnection.submitSignedTransaction(
          transactionBytes,
          crypto.sign(privateKey, transactionBytes)
        )
        // check real result
        TestHelperStrings.compareJson(testMatData, realTransactionResult)
        // check approvalResult
        transactionApprovalResult should include(argEnrollmentId)
        transactionApprovalResult should include(username)

        // test approvals on ledger
        val getResult = initializeApproval(username).getApprovals("UC4.MatriculationData", "addMatriculationData", testMatData)
        getResult should include(argEnrollmentId)
        getResult should include(username)

        // test info stored on matriculation ledger
        val storedMatData = matriculationConnection.getMatriculationData(argEnrollmentId)
        TestHelperStrings.compareJson(testMatData, storedMatData)
      }
    }
  }
  "PrintTest info " when {
    "preparing data " should {
      "not fail 1" in {
        val enrollmentId = "105"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)
        val inputMatJSon = TestDataMatriculation.validMatriculationData3("500")
        val (approvalResult, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(certificate, organisationId, inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataProposal:: $info")
      }
      "not fail 2" in {
        val enrollmentId = "106"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)
        val inputMatJSon = TestDataMatriculation.validMatriculationData4("500")
        val (approvalResult, proposalBytes) = matriculationConnection.getProposalUpdateMatriculationData(certificate, organisationId, inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataProposal:: $info")
      }
      "not fail 3" in {
        val enrollmentId = "107"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)
        val (approvalResult, proposalBytes) = matriculationConnection.getProposalAddEntriesToMatriculationData(
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
