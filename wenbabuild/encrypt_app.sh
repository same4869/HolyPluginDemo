#!/bin/bash

echo "useage: $0 SoMapping"

srcFile=../bbcomm/src/main/java/com/wenba/bangbang/so/SoMapping.java
outFile=../bbcomm/src/main/assets/files/wenba.dat

echo inFile=${srcFile}, outFile=${outFile}

java -jar libJar/encrypt.jar http://10.10.233.35:2800 $srcFile $outFile

if [ $? != 0 ]
then
	echo "error Happened on request encrypt server"
	exit
fi

echo "update success, show git status"
git status ../bbcomm/src/main/assets/files/wenba.dat
