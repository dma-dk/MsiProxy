# Danish Legacy MSI Provider #

An MSI provider for the MSI-Proxy that provides MSI messages from the Danish legacy MSI-admin production system.

## Initial setup

On the server running the MSI-Proxy, you need to set up a MySQL database with imported data from the legacy MSI-admin production system.
This entails the following steps:


### MSI-admin setup
On the MSI-admin production server, a job must be set up to export the relevant tables on a regular basis.
The tables constitute a subset of the MSI-admin database and do not contain sensitive data (users, passewords, etc).

The list of relevant tables can be seen in the following command:

    mysqldump -u <<DB_USER>> --password=<<DB_PWD>> <<DB_NAME>> \
        message priority msg_class msg_category msg_sub_category location locationtype main_area country point \
        firing_period, firing_area, firing_area_information, information, information_type, firing_area_position \
        | gzip -9 > msi-safe-dump.sql.gz

Currently, this export is made publicly available at [http://msi.dma.dk/msi-safe-dump.sql.gz](http://msi.dma.dk/msi-safe-dump.sql.gz).


### MSI-Proxy MySQL Database
Set up an "oldmsi" database with an oldmsi/oldmsi user:

    mysql -u root -p < db/create-database.sql

### MSI-Proxy Cron Job
Define a cron job for fetching the MSI-Admin export and importing it into the "oldmsi" MSI-Proxy database.

    40 * * * * /home/enav/fetch-legacy-msi.sh >> /home/enav/legacy-msi-import.log 2>&1

The `fetch-legacy-msi.sh` shell script can be found under [db/fetch-legacy-msi.sh](db/fetch-legacy-msi.sh).


