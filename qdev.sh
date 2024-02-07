#!/bin/bash

#NIORD_HOME=~/.niord-dk
NIORD_HOME=home-niord-dk

#rm -fr $NIORD_HOME
# Check if the NIORD_HOME directory does not exist
if [ ! -d "$NIORD_HOME" ]; then
    echo "Creating Niord Home"
    mkdir -p $NIORD_HOME/batch-jobs/batch-sets
    cp niord-dk-dev-basedata.zip $NIORD_HOME/batch-jobs/batch-sets/
fi


echo "Starting Niord DK"

mvn -o quarkus:dev -Dniord.home=../$NIORD_HOME -Dquarkus.jaeger.enabled=true -Dquarkus.oidc.client-id=niord-web -Dquarkus.oidc.roles.role-claim-path=resource_access/niord-web/roles