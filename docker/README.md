
Home: MsiProxy/docker

Start MySQL
===========

    cd msiproxy-mysql
    docker build -t dmadk/msi_msiproxy_mysql:v1-0 .
    docker run -d --name oldmsi dmadk/msi_msiproxy_mysql:v1-0
    cd ..
    
    
Import of MSI data
==================

    cd msiproxy-legacy-import
    docker build -t dmadk/msi_msiproxy_legacy-import:v1-0 .
    docker run -it --link oldmsi:oldmsi dmadk/msi_msiproxy_legacy-import:v1-0 &
    cd ..
    
NGINX
=====

    cd msi-proxy-nginx
    docker build -t dmadk/msi_msiproxy_nginx:v1-0 .
    docker run --name nginx-container -p 80:80 -p 443:443  -net=host -v /etc/certs:/etc/certs:ro -d dmadk/msi_msiproxy_nginx:v1-0
    cd ..
   
   copy certificate and key to: /etc/certs before running. The certicate/key must be called 
   
   e-navigation.net.crt
   e-navigation.net.key
   
   respectively.



Start Wildfly
=============

    cd msiproxy-wildfly
    ./build.sh
    docker run -e publishTwitterApiKey="CqgrxkIiBA3sC35TmoZ5F5Oru" -e publishTwitterApiSecret="xZXl9vsW3LCtX1Py6U2VqYUmyAK0GGYZ4RINFyXgNwV7PPcQip"\
     -e publishTwitterAccessToken="2829892014-kqkkQLD88xhfakDlbxY0rUPdRA72Nw14e6KED0n"\
      -e publishTwitterAccessTokenSecret="9brE9Ed6qak2UqluvvVG1CAShqaeezEUv5pqdQ5QZQlAG"\
       -e proxyBaseUri="https://msi-proxy.e-navigation.net"\
        -p 8080:8080 -p 9990:9990 --link oldmsi:oldmsi --link nginx-container:nginx-container dmadk/msi_msiproxy_wildfly:v1-0
    cd ..

    The stanza -e proxyBaseUri="https://msi-proxy.e-navigation.net" should not be used once DNS configuration has been completed
    The twitter keys points to msinm_dma for the time being 

    after deployment do following:
    
    docker ps
    
    use the container id in the following command:
    
    sudo nsenter --target $(sudo docker inspect --format {{.State.Pid}} <container id>)  --mount --uts --ipc --net --pid
    
    /wildfly/bin/add-user.sh ci <replace with manager password> --silent
    
    exit
    
    remember to fix the version number inside build.sh with a new version
    

Deploy to wildfly
=================

This procedure is complicated by the fact that wildfly is running inside a container. Instead update the docker module
with the new war.

From the msiproxy-web module, execute

    mvn wildfly:deploy -Dwildfly.hostname=<<docker up>> -Dwildfly.port=9990 -Dwildfly.username=ci -Dwildfly.password=<<PWD>>

The password is the admin user password you were asked for when you executed the ./build.sh script.


