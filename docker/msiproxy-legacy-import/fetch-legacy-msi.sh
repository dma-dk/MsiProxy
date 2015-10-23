#!/usr/bin/env bash
#
# Fetches legacy MSI data and imports them into the oldmsi database
#
while [ true ]; do

echo "Running legacy MSI import at `date`"

echo "Fetching legacy MSI"
curl http://msi.dma.dk/msi-safe-dump.sql.gz > /tmp/msi-safe-dump.sql.gz


echo "Importing data"
gunzip -c /tmp/msi-safe-dump.sql.gz | mysql -h $OLDMSI_PORT_3306_TCP_ADDR -P $OLDMSI_PORT_3306_TCP_PORT --user=oldmsi --password=oldmsi oldmsi


echo "Cleaning up"
rm -f /tmp/msi-safe-dump.sql.gz

echo ""

# update the database every 15 mins
sleep 15m

done