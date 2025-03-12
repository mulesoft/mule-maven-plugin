#!/bin/bash
###############################################################################
echo "Installing xmlstarlet"
dnf update -y --setopt=tsflags=nodocs
dnf install --setopt=tsflags=nodocs -y xmlstarlet
dnf clean all
rm -rf /var/cache/dnf/*
###############################################################################
# XPATH expressions
XPATH_SERVER='//_:servers'
XPATH_MIRROR_OF='//_:mirror/_:id[contains(text(),'nexus')]/../_:mirrorOf'
###############################################################################
DIRNAME=$(dirname $0)
SETTINGS="$HOME/.m2/settings.xml"
NEW_SETTINGS="$DIRNAME/settings-all.xml"
#Repositories that will be added, format: (repository id)||(username)||(password)
REPOSITORIES=(
  'mule-releases||${env.MULESOFT_PUBLIC_NEXUS_USER}||${env.MULESOFT_PUBLIC_NEXUS_PASS}'
  'mule-snapshots||${env.MULESOFT_PUBLIC_NEXUS_USER}||${env.MULESOFT_PUBLIC_NEXUS_PASS}'
)

echo "Coping $SETTINGS to $NEW_SETTINGS"
cp $SETTINGS $NEW_SETTINGS

echo "Editing $NEW_SETTINGS"
for REPOSITORY in "${REPOSITORIES[@]}"; do
  DATA=(${REPOSITORY//\|\|/ })
  echo "  - Adding ${DATA[0]}"
  xmlstarlet ed -L\
    -s $XPATH_SERVER -t elem -n server \
    -s "$XPATH_SERVER/server[last()]" -t elem -n id -v "${DATA[0]}" \
    -s "$XPATH_SERVER/server[last()]" -t elem -n username -v "${DATA[1]}" \
    -s "$XPATH_SERVER/server[last()]" -t elem -n password -v "${DATA[2]}" \
    "$NEW_SETTINGS"

  MIRROR_OF=$(xmlstarlet sel -t -v "$XPATH_MIRROR_OF/text()" "$NEW_SETTINGS")
  echo "  - Updating mirror with id: nexus, from '$MIRROR_OF' to '$MIRROR_OF,!${DATA[0]}'"
  xmlstarlet ed -L -u "$XPATH_MIRROR_OF" -v "$MIRROR_OF,!${DATA[0]}" "$NEW_SETTINGS"
done

echo "New settings file: $NEW_SETTINGS"
