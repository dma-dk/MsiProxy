#!/usr/bin/env bash

cp ../../install-wildfly-pom.xml .
cp ../../conf/module.xml  .
cp ../../conf/mysql-connector-java-5.1.30-bin.jar  .
cp ../../conf/standalone.xml  .
cp ../../msiproxy-web/target/msiproxy-web.war .

sed -i 's/localhost:3306/oldmsi:3306/g' standalone.xml

docker build -t dmadk/msi_msiproxy_wildfly:v1-0 .

rm -f install-wildfly-pom.xml
rm -f module.xml
rm -f mysql-connector-java-5.1.30-bin.jar
rm -f standalone.xml
rm -f msiproxy-web.war .

