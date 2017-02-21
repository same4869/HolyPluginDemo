#!/bin/bash

if [ $# = 0 ]
then
	echo "useage: $0 pluginName"
	exit
fi

echo $1

srcFile=../$1/src/main/java/com/wenba/bangbang/${1:2}/config/PluginMapping.java
outFile=../$1/src/main/assets/$1.dat

echo inFile=${srcFile}, outFile=${outFile}

java -jar libJar/encrypt.jar http://10.10.233.35:2800 $srcFile $outFile

if [ $? != 0 ]
then
	echo "error Happened on request encrypt server"
	exit
fi

echo "update success, show git status"
git status ../$1/src/main/assets/$1.dat
