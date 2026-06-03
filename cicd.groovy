pipeline {
    agent any

    environment {
      IMAGE_NAME = "kopilnagi/react-demo-docker"
      IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Cleanup Workspace') {
        steps {
          deleteDir()
        }
      }
        stage('Checkout') {
            steps {
                git branch: 'main',
                  credentialsId: 'GitHub-ssh-new',
                  url: 'git@github.com:kapilnagi007/My-First-Repo.git'
            }
        }
        
        stage('Test Docker'){
          steps {
            sh 'docker ps'
          }
        }

        stage('Debug') {
          steps {
            sh 'pwd'
            sh 'ls -la'
          }
        } 

        stage('Build Docker Image') {
          steps{
            sh 'docker build -t $IMAGE_NAME:$IMAGE_TAG -f react-demo/Dockerfile react-demo'
          }
        }

        stage('Push Image') {
          steps {
            withCredentials([
              usernamePassword(
                usernameVariable: 'DOCKER_USER',
                passwordVariable: 'DOCKER_PASS'
              )
            ])
            {
              sh '''
              echo $DOCKER_PASS | login -u $DOCKER_USER --password-stdin
              docker push $IMAGE_NAME:$IMAGE_TAG
              '''
            }
          }
        }

        stage('Deploy'){
          steps {
            sh '''
            docker stop react-app || true
            docker rm react-app || true

            docker run -d --name react-app -p 80:80 $IMAGE_NAME:$IMAGE_TAG
            '''
          }
        }
    }
}