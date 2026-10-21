pipeline {
    agent any

    tools {
        jdk 'jdk21'
    }

    environment {
        DB_URL = 'jdbc:postgresql://localhost:5432/hrpayroll'
        DB_USER = 'hrpayroll'
        DB_PASSWORD = 'hrpayroll'
        SEED_ENABLED = 'false'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean compileJava --no-daemon'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test --no-daemon'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh './gradlew bootWar --no-daemon'
                archiveArtifacts artifacts: 'build/libs/*.war', fingerprint: true
            }
        }

        stage('Report') {
            steps {
                sh './gradlew check --no-daemon || true'
            }
        }
    }

    post {
        always {
            echo "Build ${currentBuild.currentResult} for ${env.BUILD_URL}"
        }
    }
}
