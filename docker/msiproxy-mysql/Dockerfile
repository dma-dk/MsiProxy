FROM mysql

# Just set up dummy credentials - no secret data in the database
ENV MYSQL_ROOT_PASSWORD mysql
ENV MYSQL_USER oldmsi
ENV MYSQL_PASSWORD oldmsi
ENV MYSQL_DATABASE oldmsi

# Set the time zone to the local time zone
RUN echo "Europe/Copenhagen" > /etc/timezone && dpkg-reconfigure --frontend noninteractive tzdata
