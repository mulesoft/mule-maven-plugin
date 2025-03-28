#!/bin/bash
###############################################################################
DIRNAME=$(dirname $0)
###############################################################################
function EditMavenSettings() {
  echo "Installing xmlstarlet"
  dnf update -y --setopt=tsflags=nodocs
  dnf install --setopt=tsflags=nodocs -y xmlstarlet
  dnf clean all
  rm -rf /var/cache/dnf/*
  ###############################################################################
  local FILENAME="${1:-settings-all.xml}";
  local SETTINGS="$HOME/.m2/settings.xml"
  local NEW_SETTINGS="$DIRNAME/$FILENAME"
  # XPATH expressions
  local XPATH_REPOSITORIES="//_:profile/_:id[contains(text(),'nexus')]/../_:repositories"
  local XPATH_SERVER='//_:servers'
  local XPATH_MIRROR_OF="//_:mirror/_:id[contains(text(),'nexus')]/../_:mirrorOf"
  #Servers that will be added, format: (repository id)||(username)||(password)
  local SERVERS=(
    'mule-releases||${env.MULESOFT_PUBLIC_NEXUS_USER}||${env.MULESOFT_PUBLIC_NEXUS_PASS}'
    'mule-snapshots||${env.MULESOFT_PUBLIC_NEXUS_USER}||${env.MULESOFT_PUBLIC_NEXUS_PASS}'
    'mule-ci-releases||${env.MULESOFT_PUBLIC_NEXUS_USER}||${env.MULESOFT_PUBLIC_NEXUS_PASS}'
    'anypoint-exchange-v3||${env.MMP_USERNAME}||${env.MMP_PASSWORD}'
    'sfci-mule-releases||${env.NEXUS_USERNAME}||${env.NEXUS_PASSWORD}'
    'sfci-mule-snapshots||${env.NEXUS_USERNAME}||${env.NEXUS_PASSWORD}'
  )
  #Repositories that will be added, format: (id)||(url)||(release)||(snapshot)
  local REPOSITORIES=(
    'mule-ci-releases||https://repository-master.mulesoft.org/nexus/content/repositories/ci-releases||true||false'
  )

  echo "Coping $SETTINGS to $NEW_SETTINGS"
  cp $SETTINGS $NEW_SETTINGS

  echo "Editing $NEW_SETTINGS"

  echo "Set localRepository to $2"
  xmlstarlet ed -L -s "/_:settings" -t elem -n localRepository -v "$2" "$NEW_SETTINGS"

  for REPOSITORY in "${REPOSITORIES[@]}"; do
    DATA=(${REPOSITORY//\|\|/ })
    echo "  - Adding Repository ${DATA[0]}"
    xmlstarlet ed -L\
      -s "$XPATH_REPOSITORIES" -t elem -n repository \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n id -v "${DATA[0]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n name -v "${DATA[0]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n url -v "${DATA[1]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n releases \
      -s "$XPATH_REPOSITORIES/repository[last()]/releases[last()]" -t elem -n enabled -v "${DATA[2]}" \
      -s "$XPATH_REPOSITORIES/repository[last()]" -t elem -n snapshots \
      -s "$XPATH_REPOSITORIES/repository[last()]/snapshots[last()]" -t elem -n enabled -v "${DATA[3]}" \
      "$NEW_SETTINGS"
  done

  for SERVER in "${SERVERS[@]}"; do
    DATA=(${SERVER//\|\|/ })
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

  echo "New settings file: $NEW_SETTINGS"
}
###############################################################################
function InstallMaven() {
  local VERSION="${1/MVN-/}"
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

  mkdir -p /usr/share/maven /usr/share/maven/ref \
    && curl -fsSL -o /tmp/apache-maven.tar.gz https://archive.apache.org/dist/maven/maven-3/${VERSION}/binaries/apache-maven-${VERSION}-bin.tar.gz \
    && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

  export MAVEN_HOME=/usr/share/maven
  mvn -v
}
###############################################################################
function CopySettings() {
  local SETTINGS="$HOME/.m2/settings.xml"
  local FILENAME="${1:-settings-all.xml}";
  local NEW_SETTINGS="$DIRNAME/$FILENAME"

  echo "Coping maven settings from $NEW_SETTINGS to $SETTINGS"
  cp "$NEW_SETTINGS" "$SETTINGS"
}
############################################################
############################################################
# Main program                                             #
############################################################
############################################################
getopts ":smc" option
case $option in
  s) # Create Maven setting file
     EditMavenSettings "$2" "$3";;
  m) # Install Maven
     InstallMaven "$2";;
  c) # Copy maven settings to the user home
     CopySettings "$2";;
 \?) # Invalid option
     echo "Error: Invalid option"
     exit;;
esac
