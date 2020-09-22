# Scala Hyperleger API

## Current Status
![Tests with Dev Network](https://github.com/upb-uc4/hlf-api/workflows/Hyperledger_Scala_With_Dev_Network/badge.svg)

![Tests with Production Network](https://github.com/upb-uc4/hlf-api/workflows/Hyperledger_Scala_With_Production_Network/badge.svg)

![Code Format](https://github.com/upb-uc4/hlf-api/workflows/Code%20Format%20Check%20Pipeline/badge.svg)

## Prerequesites

1. Have a working UC4-chaincode-network running 
    (download from [dev-network](https://github.com/upb-uc4/hlf-dev-network)
    and  [chaincode](https://github.com/upb-uc4/hlf-chaincode)
    )
2. Store the .yaml describing the network (example provided in ./src/test/resources/connection_profile.yaml)
3. Store the wallet-directory containing the certificate (example for dev-network provided in .src/test/resource/wallet/cli.id)

## Configuration / Initialization

### 1. Dependencies
```sbt
val hyperledgerApiVersion = "v0.8.1"
val hyperledger_api = RootProject(uri("https://github.com/upb-uc4/hlf-api.git#%s".format(hyperledgerApiVersion)))

lazy val yourProject = (project in file(".")).dependsOn(hyperledger_api)
```
### 2. Imports
- the Connections (Class and Trait) you want to access
```scala
import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionCourses, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCourseTrait, ConnectionMatriculationTrait }
```
- the Managers you need service from
```scala
import de.upb.cs.uc4.hyperledger.utilities.EnrollmentManager
import de.upb.cs.uc4.hyperledger.utilities.RegistrationManager
```


## Communicate with the Network

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
protected val caURL: String = "172.17.0.3:30906" // adress of the CA-server.

protected val username: String = "TestUser123" // this should in most cases be the name of the .id file in your wallet directory.
protected val password: String = "Test123" // a password used to register a user and receive/set a certificate for said user when enrolling.
protected val organisationId: String = "org1MSP" // the name of the organisation the user belongs to.

```

### 0.5 (optional) Register a user (only possible if you already obtained an admin certificate through the enrollment-process)
```scala
val newUserName = "TestUser123" // new user to be registered.
val adminUserName = "scala-registration-admin-org1" // current existing adminEntity in our production network.
val organisationName = "org1" // current organisation name in our production network.
val maxEnrollments = 1 // number of times the user can be enrolled/reenrolled with the same username-password combination (default = 1)
val newUserType = HFCAClient.HFCA_TYPE_CLIENT // permission level of the new user (default = HFCAClient.HFCA_TYPE_CLIENT)

val newUserPassword = RegistrationManager.register(tlsCert, caURL, newUserName, adminUserName, walletPath, organisationName, maxEnrollments, )
```

### 1. Enrollment 
```scala
val newUserName = "TestUser123" // new user to be enrolled
val newUserPassword = "Test123" // new user password (retrieve from registration-process)
val organisationId = "org1MSP" // id of the organisation the user belongs to (current production network organisation is "org1MSP")
EnrollmentManager.enroll(caURL, tlsCert, walletPath, newUserName, newUserPassword, organisationId)
```

### 2. Connection Initilization
Simply create an object of the connection for the contract that you want to access
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
