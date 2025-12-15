#!groovy

def workerNode = "devel12"

pipeline {
	agent {label workerNode}
	options {
		timestamps()
	}
	tools {
        maven "Maven 3"
    }
	stages {
		stage("clear workspace") {
			steps {
				deleteDir()
				checkout scm
			}
		}
        stage("build") {
            steps {
                withSonarQubeEnv(installationName: 'sonarqube.dbc.dk') {
                    script {
                        def status = sh returnStatus: true, script:  """
                            rm -rf \$WORKSPACE/.repo/dk/dbc
                            mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo --no-transfer-progress clean
                            mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo --no-transfer-progress verify
                        """
                        
                        def sonarOptions = "-Dsonar.branch.name=$BRANCH_NAME"
                        if (env.BRANCH_NAME != 'master') {
                            sonarOptions += " -Dsonar.newCode.referenceBranch=master"
                        }

                        status += sh returnStatus: true, script: """
                            mvn -B -Dmaven.repo.local=$WORKSPACE/.repo --no-transfer-progress $sonarOptions sonar:sonar
                        """

                        junit testResults: '**/target/surefire-reports/TEST-*.xml'

                        if (status != 0) {
                            error("build failed")
                        }
                    }
                }
            }
        }
        stage("quality gate") {
            steps {
                // wait for analysis results
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
		stage("deploy") {
			when {
				branch "master"
			}
			steps {
				sh "mvn jar:jar deploy:deploy"
			}
		}
	}
}
