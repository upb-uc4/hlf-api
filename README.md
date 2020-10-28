# Scala Hyperledger API

## Current Status
![Tests with Dev Network](https://github.com/upb-uc4/hlf-api/workflows/Hyperledger_Scala_With_Dev_Network/badge.svg)

![Tests with Production Network](https://github.com/upb-uc4/hlf-api/workflows/Hyperledger_Scala_With_Production_Network/badge.svg)

![Code Format](https://github.com/upb-uc4/hlf-api/workflows/Code%20Format%20Check%20Pipeline/badge.svg)

[Maven](https://search.maven.org/artifact/de.upb.cs.uc4/hlf-api)

## Prerequisites

1. Have a working UC4-chaincode-network running 
    (download from [dev-network](https://github.com/upb-uc4/hlf-dev-network)
    and  [chaincode](https://github.com/upb-uc4/hlf-chaincode)
    )
2. Store the .yaml describing the network (example provided in ./src/test/resources/connection_profile.yaml)
3. (Optional) Store the certificates you want to use in your wallet-directory (example for dev-network provided in .src/test/resource/wallet/cli.id)

## Configuration / Initialization

### 1. Dependencies [Maven](https://search.maven.org/artifact/de.upb.cs.uc4/hlf-api)
```sbt
val hlf_api_version = "0.10.2"
val hlf_api = "de.upb.cs.uc4" % "hlf-api" % hlf_api_version

lazy val yourProject = (project in file(".")).dependencies(hyperledger_api)
```
### 2. Imports
- the Connections (Class and Trait) you want to access
```scala
import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionMatriculation
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
```
- the Managers you need service from
```scala
import de.upb.cs.uc4.hyperledger.utilities.EnrollmentManager
import de.upb.cs.uc4.hyperledger.utilities.RegistrationManager
```


## Communicate with the Network

Any Communication with the Network will happen through our designated interface and methods provided.
These Methods can throw different types of Exceptions as described in 
https://github.com/upb-uc4/api/blob/develop/hlf_scala_api_errors.md
In General these are
- TransactionException :: you invoked the chaincode/contract in a wrong/invalid manner
- NetworkException :: you could not build a connection to the specified network.
- HyperledgerException :: something unexpected happened with the HLF framework



### 0. Configure your connection variables (These are used to let the framework know how to access YOUR UC4-hlf-network)
- general information on the network
```scala
protected val walletPath: Path = "/hyperledger_assets/wallet/" // the directory containing your certificates.
protected val networkDescriptionPath: Path = "/hyperledger_assets/connection_profile.yaml" // the file describing the existing network.
protected val channel: String = "myc" // name of the shared channel a connection is requested for.
protected val chaincode: String = "mycc" // name of the chaincode a connection is requested for.
```

- for user-management
```scala
protected val tlsCert: Path = "/hyperledger_assets/ca_cert.pem" // CA-certificate to have your client validate that the Server you are talking to is actually the CA.
protected val caURL: String = "172.17.0.3:30906" // address of the CA-server.

protected val username: String = "TestUser123" // this should in most cases be the name of the .id file in your wallet directory.
protected val password: String = "Test123" // a password used to register a user and receive/set a certificate for said user when enrolling.
protected val organisationId: String = "org1MSP" // the id of the organisation the user belongs to.
protected val organisationName: String = "org1" // the name of the organisation the user belongs to.

```

### 0.5 (optional) Register a user (only possible if you already obtained an admin certificate through the enrollment-process)
```scala
val enrollmentId: String = "TestUser123" // new user to be registered.
val adminUserName: String = "scala-registration-admin-org1" // current existing adminEntity in our production network.
val organisationName: String = "org1" // current organisation name in our production network.
val maxEnrollments: Integer = 1 // number of times the user can be enrolled/re-enrolled with the same username-password combination (default = 1)
val newUserType: String = HFCAClient.HFCA_TYPE_CLIENT // permission level of the new user (default = HFCAClient.HFCA_TYPE_CLIENT)

val newUserPassword: String = RegistrationManager.register(tlsCert, caURL, newUserName, adminUserName, walletPath, organisationName, maxEnrollments)
```

### 1. Enrollment 
Any Enrollment "publishes" your newly registered user.
This user can be enrolled with his enrollmentId and enrollmentSecret.
```scala
val enrollmentId: String = "TestUser123" // new user to be enrolled
val enrollmentSecret: String = "Test123" // new user password (retrieve from registration-process)
```

It Creates/Signs your Certificate and stores the signedCertificate on the ledger.
For this we need to pass the following additional parameters.
```scala
val channel: String = "myc"/"mychannel" // channel your certificate-chaincode is setup for.
val chaincode: String = "mycc"/ // chaincode containing your certificate-contract
protected val networkDescriptionPath: Path = "/hyperledger_assets/connection_profile.yaml" // the file describing the existing network.
```

#### 1.1 Basic Enrollment (generates your KeyPair and stores it in your wallet)
This enrollment generates a new KeyPair for the newly enrolled user.
This means we need to store the new X509Identity (configured with an organisationId) in a wallet.

```scala
val organisationId: String = "org1MSP" // id of the organisation the user belongs to (current production network organisation is "org1MSP")
val walletPath: Path = "/hyperledger_assets/wallet/" // directory your X509Identity will be stored in.
```

Now the basic enrollment can be preformed.
```scala
EnrollmentManager.enroll(caURL, tlsCert, walletPath, enrollmentId, enrollmentSecret, organisationId, channel, chaincode, networkDescriptionPath)
```

#### 1.2 Secure Enrollment (Sign your CSR)
To have the enrollment sign your provided Certificate Signing Request, you need to pass said CSR.
```scala
val CSR: String = "Some CSR String" // passed "Certificate Signing Request".
```

```scala
val adminName: String = "scala-registration-admin-org1" // admin identity used to store the signedCertificate on the Ledger.
val adminWalletPath: Path = "/hyperledger_assets/wallet/" // directory containing your admin certificate.
```

Now the secure enrollment can be preformed.
```scala
val signedCertificate: String = EnrollmentManager.enrollSecure(caURL, tlsCert, enrollmentId, enrollmentSecret, adminName, adminWalletPath, channel, chaincode, networkDescriptionPath)
```

### 2. Connection Initialization
Simply create an object of the connection for the contract that you want to access and provide the credentials for your username in the given wallet.
```scala
def createConnection: ConnectionMatriculationTrait =
  de.upb.cs.uc4.hyperledger.connections.cases.ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)
```


### 3. Performing Transactions
```scala
try {
    val result = connection.addEntryToMatriculationData(matriculationId, fieldOfStudy, semester)
} catch {
    case e_t: TransactionException => HandleError(e_t) // The transaction you have called seems to be invalid. Please refer to e_t.payload for a detailed message.
    case e_h: HyperledgerInnerException => HandleError(e_h) // something seems to have gone wrong with the framework, please submit a bugReport :)
}
```

### [4. All Connections and Transactions](https://github.com/upb-uc4/api/tree/develop/hlf/scala)
