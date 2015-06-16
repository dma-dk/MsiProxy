FROM ubuntu:latest

# Install mysql client
RUN apt-get -q -y install mysql-client curl

# Add the legacy import script called by the cron job
ADD fetch-legacy-msi.sh /

RUN chmod 755 /fetch-legacy-msi.sh

# Set the time zone to the local time zone
RUN echo "Europe/Copenhagen" > /etc/timezone && dpkg-reconfigure --frontend noninteractive tzdata

# Run the command on container startup
CMD [ "/bin/bash", "/fetch-legacy-msi.sh" ]
