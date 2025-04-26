#!/bin/bash
set -e # Exit immediately if a command exits with a non-zero status.
###############################################################################
DIRNAME=$(dirname $0)
###############################################################################
# Arguments:
# - $1: Original setting.xml path
# - $2: Name of the final file
# - $3: Path of the repository
function EditMavenSettings() {
  echo "Installing xmlstarlet"
  dnf update -y --setopt=tsflags=nodocs
  dnf install --setopt=tsflags=nodocs -y xmlstarlet
  dnf clean all
  rm -rf /var/cache/dnf/*
  ###############################################################################
  local FILENAME="${2:-settings-all.xml}";
  local SETTINGS="$1"
  local NEW_SETTINGS="$DIRNAME/$FILENAME"
  # XPATH expressions
  local XPATH_REPOSITORIES="//_:profile/_:id[contains(text(),'nexus')]/../_:repositories"
  local XPATH_SERVER='//_:servers'
  local XPATH_MIRROR_OF="//_:mirror/_:id[contains(text(),'nexus')]/../_:mirrorOf"
  #Servers that will be added, format: (repository id)||(username)||(password)
  local SERVERS=(
    # Internal repositories (SF) to resolve dependencies
    'mulesoft-maven-all||${env.NEXUS_USERNAME}||${env.NEXUS_PASSWORD}'
    'mulesoft-maven-external-releases||${env.NEXUS_USERNAME}||${env.NEXUS_PASSWORD}'
    'mulesoft-maven-external-snapshots||${env.NEXUS_USERNAME}||${env.NEXUS_PASSWORD}'
    # Exchange repositories required for the integrations tests
    'anypoint-exchange-v3||${env.MMP_USERNAME}||${env.MMP_PASSWORD}'
    # External repository for publish
    'mulesoft-master||${MULESOFT_USERNAME}||${MULESOFT_PASSWORD}'
  )
  #Repositories that will be added, format: (id)||(url)||(release)||(snapshot)
  local REPOSITORIES=(
    'mulesoft-maven-all||${env.NEXUS_BASE_URL}/groups/mulesoft-maven-all/||true||true'
    'mulesoft-maven-external-releases||${env.NEXUS_BASE_URL}/repositories/mulesoft-maven-external-releases/||true||false'
    'mulesoft-maven-external-snapshots||${env.NEXUS_BASE_URL}/repositories/mulesoft-maven-external-snapshots/||false||true'
  )
  ##
  echo "Coping $SETTINGS to $NEW_SETTINGS"
  cp $SETTINGS $NEW_SETTINGS

  echo "Editing $NEW_SETTINGS"
  ##
  echo "Set localRepository to $3"
  xmlstarlet ed -L -s "/_:settings" -t elem -n localRepository -v "$3" "$NEW_SETTINGS"
  ##
  for REPOSITORY in "${REPOSITORIES[@]}"; do
    local DATA=(${REPOSITORY//\|\|/ })
    echo "  - Adding Repository ${DATA[0]}"
    xmlstarlet ed -L\
      -s "$XPATH_REPOSITORIES" -t elem -n repository \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n id -v "${DATA[0]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n name -v "${DATA[0]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n url -v "${DATA[1]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n releases \
      -s "$XPATH_REPOSITORIES/repository[last()]/releases[last()]" -t elem -n enabled -v "${DATA[2]}"\
      -s "$XPATH_REPOSITORIES/repository[last()]/releases[last()]" -t elem -n updatePolicy -v "always" \
      -s "$XPATH_REPOSITORIES/repository[last()]/releases[last()]" -t elem -n checksumPolicy -v "warn" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n snapshots \
      -s "$XPATH_REPOSITORIES/repository[last()]/snapshots[last()]" -t elem -n enabled -v "${DATA[3]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]/snapshots[last()]" -t elem -n updatePolicy -v "never" \
      -s "$XPATH_REPOSITORIES/repository[last()]/snapshots[last()]" -t elem -n checksumPolicy -v "fail" \
      "$NEW_SETTINGS"
  done
  ##
  for SERVER in "${SERVERS[@]}"; do
    local DATA=(${SERVER//\|\|/ })
    echo "  - Adding Server ${DATA[0]}"
    xmlstarlet ed -L\
      -s "$XPATH_SERVER" -t elem -n server \
      -s "$XPATH_SERVER/server[last()]" -t elem -n id -v "${DATA[0]}" \
      -s "$XPATH_SERVER/server[last()]" -t elem -n username -v "${DATA[1]}" \
      -s "$XPATH_SERVER/server[last()]" -t elem -n password -v "${DATA[2]}" \
      "$NEW_SETTINGS"

      MIRROR_OF=$(xmlstarlet sel -t -v "$XPATH_MIRROR_OF/text()" "$NEW_SETTINGS")
      echo "  - Updating mirror with id: nexus, from '$MIRROR_OF' to '$MIRROR_OF,!${DATA[0]}'"
      xmlstarlet ed -L -u "$XPATH_MIRROR_OF" -v "$MIRROR_OF,!${DATA[0]}" "$NEW_SETTINGS"
  done
  ##
  echo "New settings file: $NEW_SETTINGS"
}
###############################################################################
# Arguments:
# - $1: Maven Version
function InstallMaven() {
  local VERSION="${1/MVN-/}"
  local NEXUS_URL="${NEXUS_BASE_URL:-https://nexus-proxy.repo.local.sfdc.net/nexus/content}";
  local FULL_URL="${NEXUS_URL}/repositories/central/org/apache/maven/apache-maven/${VERSION}/apache-maven-${VERSION}-bin.tar.gz"
  ####
  case $VERSION in
    3.8.8) local SHA=332088670d14fa9ff346e6858ca0acca304666596fec86eea89253bd496d3c90deae2be5091be199f48e09d46cec817c6419d5161fb4ee37871503f472765d00;;
    3.9.0) local SHA=1ea149f4e48bc7b34d554aef86f948eca7df4e7874e30caf449f3708e4f8487c71a5e5c072a05f17c60406176ebeeaf56b5f895090c7346f8238e2da06cf6ecd;;
    3.9.4) local SHA=deaa39e16b2cf20f8cd7d232a1306344f04020e1f0fb28d35492606f647a60fe729cc40d3cba33e093a17aed41bd161fe1240556d0f1b80e773abd408686217e;;
    3.9.6) local SHA=706f01b20dec0305a822ab614d51f32b07ee11d0218175e55450242e49d2156386483b506b3a4e8a03ac8611bae96395fd5eec15f50d3013d5deed6d1ee18224;;
    3.9.9) local SHA=a555254d6b53d267965a3404ecb14e53c3827c09c3b94b5678835887ab404556bfaf78dcfe03ba76fa2508649dca8531c74bca4d5846513522404d48e8c4ac8b;;
        *)  echo "Error: Invalid maven version [$2, $VERSION]"
            exit;;
  esac
  ####
  echo "Installing Maven $VERSION"
  dnf remove -y maven
  echo "Download maven from: ${FULL_URL}"
  mkdir -p /usr/share/maven /usr/share/maven/ref \
    && curl -fsSL -u ${NEXUS_USERNAME}:${NEXUS_PASSWORD} -o /tmp/apache-maven.tar.gz ${FULL_URL}\
    && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

  export MAVEN_HOME=/usr/share/maven
  export PATH=$PATH:/usr/share/maven/bin/mvn
  mvn -v
}
############################################################
############################################################
# Main program                                             #
############################################################
############################################################
getopts ":sm" option
case $option in
  s) # Create Maven setting file
     EditMavenSettings "$2" "$3" "$4";;
  m) # Install Maven
     InstallMaven "$2";;
 \?) # Invalid option
     echo "Error: Invalid option"
     exit;;
esac
