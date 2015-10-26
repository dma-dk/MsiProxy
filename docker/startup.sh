#!/usr/bin/env bash
# spins up the system, remember to save certificates before and set administrative password after
# also twitter tokens and proxyBaseUrl should be fixed for real production
# add user to docker group
# sudo usermod -g docker <user>
# docker rm <id> with oldmsi id to prevent name conflicts on second startup
docker kill $(docker ps | awk '{print $1}')
docker rm $(docker ps -a | awk '{print $1}')
docker run -d --name oldmsi dmadk/msi_msiproxy_mysql:v1-0
sleep 10
docker run -p 80:80 -p 443:443  --net=host -v /etc/certs:/etc/certs:ro -d dmadk/msi_msiproxy_nginx:v1-0
docker run -d -it --link oldmsi:oldmsi dmadk/msi_msiproxy_legacy-import:v1-0
sleep 15
docker run -d -e publishTwitterApiKey="CqgrxkIiBA3sC35TmoZ5F5Oru" -e publishTwitterApiSecret="xZXl9vsW3LCtX1Py6U2VqYUmyAK0GGYZ4RINFyXgNwV7PPcQip"\
     -e publishTwitterAccessToken="2829892014-kqkkQLD88xhfakDlbxY0rUPdRA72Nw14e6KED0n"\
      -e publishTwitterAccessTokenSecret="9brE9Ed6qak2UqluvvVG1CAShqaeezEUv5pqdQ5QZQlAG"\
       -e proxyBaseUri="https://msi-proxy.e-navigation.net"\
        -p 8080:8080 -p 9990:9990 --link oldmsi:oldmsi dmadk/msi_msiproxy_wildfly:v1-0