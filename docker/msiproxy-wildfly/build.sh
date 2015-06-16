
echo "Please Admin user password"
read PWD

sed 's/MANAGEMENT_PWD/'$PWD'/g' Dockerfile-template > Dockerfile

cp ../../install-wildfly-pom.xml .
cp ../../conf/module.xml  .
cp ../../conf/mysql-connector-java-5.1.30-bin.jar  .
cp ../../conf/standalone.xml  .

sed -i 's/localhost:3306/oldmsi:3306/g' standalone.xml

docker build -t test/msiproxy-wildfly .

rm -f install-wildfly-pom.xml
rm -f module.xml
rm -f mysql-connector-java-5.1.30-bin.jar
rm -f standalone.xml
rm -f Dockerfile

