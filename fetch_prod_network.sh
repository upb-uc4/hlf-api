#!/bin/bash


#clone production network

if [ ! -d ./hyperledger_chaincode/ ]
then
	git clone https://github.com/upb-uc4/hlf-network.git
else
	read -p "Update existing production network? " -n 1 -r
	if [[ $REPLY =~ ^[Yy]$ ]]
	then
		pushd ./hlf-network
		git pull
		popd
	fi
fi

echo "#############################################"
echo "#       production network up to date       #"
echo "#############################################"
