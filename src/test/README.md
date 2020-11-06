# Scala Hyperledger API - Running Tests

## Prerequisites

1. Have a working UC4-chaincode-network running 
    (download from [hlf-network](https://github.com/upb-uc4/hlf-network)
    and  [chaincode](https://github.com/upb-uc4/hlf-chaincode)
    )
2. Set up the Environment Variable referencing the "connection_profile.yaml" like 
```shell script
    export UC4_CONNECTION_PROFILE ='./hlf-network/assets/connection_profile_kubernetes_local.yaml'
```
3. Set up the Environment Variable describing what kind of Network you are using like
```shell script
export UC4_TESTBASE_TARGET='PRODUCTION_NETWORK'
```
4. Set up the Environment Variable to find the Certificate Authority like 
```shell script
export UC4_KIND_NODE_IP=$(source .hlf-network/scripts/util.sh && get_worker_ip)
```

## Run the Tests :)

```sbt
sbt testOnly "*ApprovalTests *CertificateErrorTests"
```