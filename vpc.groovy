def call() {
    pipeline {
        agent{
            node{
                label "tf-provision"
            }
        }

        environment{
            def TF_PATH = "${WORKSPACE}/jenkins-pipeline/"
            def GITHUB_REPO = "terraform-provisioning-jenkins"
            def GITHUB_URL = "https://github.com/punknotdie/${GITHUB_REPO}.git"
            def DEV_DEPLOYMENT = "dev-vpc-provisioning"
            def PROD_DEPLOYMENT = "prod-vpc-provisioning"
        }

        stages {
            stage("Git Clone"){
                steps {
                    script {
                        NOTIF_MSG="Build #${BUILD_NUMBER} on stage Git Clone"
                        git branch: "${BRANCH_NAME}",
                        credentialsId: "GITHUB_SECRET",
                        url: "${GITHUB_URL}"                     
                    }
                }
            }
            stage ("Deploy Terraform"){
                steps {
                    script {
                        if(env.BRANCH_NAME == "dev/provisioning") {
                            sh """
                            cd ${WORKSPACE}/resources/${DEV_DEPLOYMENT}
                            terraform init
                            terraform plan
                            sleep 5
                            terraform apply -auto-approve
                            """
                        } else if (env.BRANCH_NAME == "prod/provisioning"){
                            sh """
                            cd ${WORKSPACE}/resources/${PROD_DEPLOYMENT}
                            terraform init
                            terraform plan
                            sleep 5
                            terraform apply -auto-approve
                            """ 
                        }
                    }
                }
            }
            stage ("Check State"){
                steps {
                    script {
                        if(env.BRANCH_NAME == "dev/provisioning") {
                            sh "cat ${WORKSPACE}/resources/${DEV_DEPLOYMENT}/terraform.tfstate"
                        } else if (env.BRANCH_NAME == "prod/provisioning"){
                            sh "cat ${WORKSPACE}/resources/${PROD_DEPLOYMENT}/terraform.tfstate"
                        }
                    }
                }
            }
  
        }
    }
}
