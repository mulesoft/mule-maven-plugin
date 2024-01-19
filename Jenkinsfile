    @Library('lifecycle-utils@master') _

    Map pipelineParams = [
            "agent": "ubuntu-14.04",
            "jdk": "JDK8",
            "maven": "Maven (latest)",
            "projectKey": "mule-maven-plugin",
            "mavenAdditionalArgs": "",
            "deployArtifacts": true,
            "binariesScan": true,
            "skipTestsArgs": "-DskipIntegrationTests"
    ]

    def CURRENT_STAGE

    pipeline {
        options {
            timeout(time: 10, unit: 'HOURS')
            buildDiscarder(logRotator(numToKeepStr: '5'))
        }

        tools {
            jdk pipelineParams.jdk
            maven pipelineParams.maven
        }

        agent {
            label pipelineParams.agent
        }

        stages {
            stage('Build') {
                steps {
                    script {
                        CURRENT_STAGE = env.STAGE_NAME
                        buildWithMaven("clean install ${pipelineParams.skipTestsArgs} ${pipelineParams.mavenAdditionalArgs}")
                    }
                }
            }
            stage('Binaries Scan') {
                tools {
                    jdk "adoptopenjdk-17"
                }
                when {
                  expression { pipelineParams.binariesScan }
                }
                steps {
                    script {
                        CURRENT_STAGE = env.STAGE_NAME
                        scanSonarQube(pipelineParams.projectKey)
                        scanNexusIQ()
                    }
                }
            }
            stage('Deploy Artifacts') {
                steps {
                    script {
                        CURRENT_STAGE = env.STAGE_NAME
                        if (isUnix() && !isDevBranch()) {
                            echo "Performing artifacts deployment..."
                            buildWithMaven("clean deploy -DskipTests -P '!default'")
                        } else {
                            echo "Artifacts deployment skipped..."
                        }
                    }
                }
            }
        }
        post {
            failure {
                notifyBuildToSlack(CURRENT_STAGE)
            }
        }
    }