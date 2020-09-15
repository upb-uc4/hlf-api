#!/bin/bash


#clone dev network

if [ ! -d ./hlf-dev-network/ ]
then
	git clone https://github.com/upb-uc4/hlf-dev-network.git
else
	read -p "Update existing dev network? " -n 1 -r
	if [[ $REPLY =~ ^[Yy]$ ]]
	then
		pushd ./hlf-dev-network
		git pull
		popd
	fi
fi

echo "#############################################"
echo "#         dev network up to date            #"
echo "#############################################"