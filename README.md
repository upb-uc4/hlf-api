# Scala Hyperleger API

## Current Status
![Tests with Dev Network](https://github.com/upb-uc4/hlf-api/workflows/Hyperledger_Scala_With_Dev_Network/badge.svg)

![Tests with Production Network](https://github.com/upb-uc4/hlf-api/workflows/Hyperledger_Scala_With_Production_Network/badge.svg)

![Code Format](https://github.com/upb-uc4/hlf-api/workflows/Code%20Format%20Check%20Pipeline/badge.svg)

## Prerequesites

1. Have a working UC4-chaincode-network running 
    (download from [dev_network](https://github.com/upb-uc4/University-Credits-4.0/tree/develop/product_code/hyperledger/dev_network)
    and  [chaincode](https://github.com/upb-uc4/University-Credits-4.0/tree/develop/product_code/hyperledger/chaincode)
    )
2. Store the .yaml describing the network (example provided in ./connection_profile.yaml)
3. Store the wallet-directory containing the certificate (example provided in ./wallet/cli.id)

## Configuration / Initialization

1. Add the dependencies to your scala project
```
val hyperledgerApiVersion = "v0.6.1"
val hyperledger_api = RootProject(uri("https://github.com/upb-uc4/hlf-api.git#%s".format(hyperledgerApiVersion)))

lazy val yourProject = (project in file(".")).dependsOn(hyperledger_api)
```
2. Import the Manager and the ConnectionObject Traits and Classes
```
import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait
import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionMatriculation
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
```

## Communicate with the Network

0. Configure your connection variables
```
protected val walletPath: Path = "/hyperledger_assets/wallet/"
protected val networkDescriptionPath: Path = "/hyperledger_assets/connection_profile.yaml"
protected val tlsCert: Path = "/hyperledger_assets/ca_cert.pem"

protected val username: String = "user123
protected val password: String = "pw123"
protected val organisationId: String = "myOrg123"

protected val channel: String = "myc"
protected val chaincode: String = "mycc"
protected val caURL: String = "172.17.0.3:30906"
```

1. Enroll with your password and name to get your certificate
```
EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId)
```

2. Initialize a connection to the network
```
def createConnection: ConnectionMatriculationTrait =
  de.upb.cs.uc4.hyperledger.connections.cases.ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)
```

3. Pass commands to the Connection
```
try {
    val result = connection.addEntryToMatriculationData(matriculationId, fieldOfStudy, semester)
} catch {
    case e: Exception => HandleError(e)
}
```
