@Library('lifecycle-utils@master') _
//Library code can be found at https://github.com/mulesoft/lifecycle-pipeline-utils


def pipelineParams = [
  "agent": "ubuntu-14.04",
  "jdk": "JDK8",
  "maven": "M3"
]


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
            stage('Prepare env') {
                steps {
                    buildWithMaven("install -DskipTests")
                }
            }
            stage('Deployer IT') {
                steps {
                    script{
                        try {
                            withCredentials([usernamePassword(credentialsId: 'mmp-tests-credentials', passwordVariable: 'mmp_password', usernameVariable: 'mmp_user')]) {
                                buildWithMaven("test -X -U -PdeployerIT -pl :mule-deployer-it -Dusername=$mmp_user -Dpassword=$mmp_password")
                            }
                        }
                        catch (Exception e) {
                            currentBuild.result = 'UNSTABLE'
                            notifyBuildToSlack(env.STAGE_NAME)
                        }
                    }
                }
            }
        }
    }