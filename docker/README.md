
Important: Currently only used for test purposes
=================================================

Start MySQL
===========

    cd msiproxy-mysql
    docker build -t test/msiproxy-mysql .
    docker run -d --name oldmsi test/msiproxy-mysql
    
    
Import of MSI data
==================

    cd msiproxy-legacy-import
    docker build -t test/msiproxy-legacy-import .
    
Set up cron job to run the import periodically

    docker run -it --link oldmsi:oldmsi test/msiproxy-legacy-import
    

Start Wildfly
=============

    ./build.sh
    docker run -it -p 8080:8080 -p 9990:9990 --link oldmsi:oldmsi test/msiproxy-wildfly
    

Deploy to wildfly
=================

From the msiproxy-web module, execute

    mvn wildfly:deploy -Dwildfly.hostname=<<docker up>> -Dwildfly.port=9990 -Dwildfly.username=ci -Dwildfly.password=<<PWD>>

The password is the admin user password you were asked for when you executed the ./build.sh script.
