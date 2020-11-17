# Scala Hyperledger API - Running Tests

## Prerequisites
0. Have a working UC4-chaincode-network running 
    (download from [hlf-network](https://github.com/upb-uc4/hlf-network)
    and  [chaincode](https://github.com/upb-uc4/hlf-chaincode)
    or use the fetch-scripts)

0.1. fetch the latest network
```sh
./fetch_prod_network.sh
```

0.2. install faketime
```sh
sudo apt-get install faketime
```

0.3 prepare folders for kind
```sh
sudo mkdir -p /data/development/hyperledger
sudo chmod -R 777 /data/development
```

0.3. start the network
Note:  use the testing flag to have the network create the testing identities.
```sh
./restart.sh -t
```

1. Set up the Environment Variable referencing the "connection_profile.yaml" like 
```shell script
    export UC4_CONNECTION_PROFILE ='<hlf-network-path(e.g. ./hlf-network)>/assets/connection_profile_kubernetes_local.yaml'
```
2. Set up the Environment Variable describing what kind of Network you are using like
```shell script
export UC4_TESTBASE_TARGET='PRODUCTION_NETWORK'
```
3. Set up the Environment Variable to find the Certificate Authority like 
```shell script
export UC4_KIND_NODE_IP=$(source <hlf-network-path(e.g. ./hlf-network)>/scripts/util.sh && get_worker_ip)
```

## Run the Tests :)

```sbt
sbt testOnly "*ApprovalTests *CertificateErrorTests"
```
