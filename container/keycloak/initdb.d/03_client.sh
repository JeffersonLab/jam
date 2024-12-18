#!/bin/bash

. /lib.sh

echo "-----------------"
echo "| Create Client |"
echo "-----------------"
# KEYCLOAK_RESOURCE set in 00_config.env as it's a shared value
KEYCLOAK_CLIENT_NAME=jam
KEYCLOAK_SERVICE_ACCOUNT_ENABLED=true
KEYCLOAK_REDIRECT_URIS='["https://localhost:8443/jam/*"]'
KEYCLOAK_SECRET=yHi6W2raPmLvPXoxqMA7VWbLAA2WN0eB
create_client

# Wildfly Elytron client legacy adapter fix
/opt/keycloak/bin/kcadm.sh update clients/${KEYCLOAK_CLIENT_NAME} -r $KEYCLOAK_REALM -s 'attributes."exclude.issuer.from.auth.response"=true'