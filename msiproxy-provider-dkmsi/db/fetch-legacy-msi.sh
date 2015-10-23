#!/usr/bin/env bash
#
# Fetches legacy MSI data and imports them into the oldmsi database
#

echo "Running legacy MSI import at `date`"

echo "Fetching legacy MSI"
curl http://msi.dma.dk/msi-safe-dump.sql.gz > /tmp/msi-safe-dump.sql.gz


echo "Importing data"
gunzip -c /tmp/msi-safe-dump.sql.gz | /usr/bin/mysql --user=oldmsi --password=oldmsi oldmsi


echo "Cleaning up"
rm -f /tmp/msi-safe-dump.sql.gz

echo ""

