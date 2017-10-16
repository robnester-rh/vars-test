package com.robnester.ci.utils

import groovy.json.JsonSlurper


/**
 * Initialize message audit file
 * @param auditFile audit file for messages
 * @return
 */
def initializeAuditFile(String auditFile) {
    // Ensure auditFile is available
    sh "rm -f " + auditFile
    String msgAuditFileDir = sh(script: 'dirname ' + auditFile, returnStdout: true).trim()
    sh 'mkdir -p ' + msgAuditFileDir
    sh 'touch ' + auditFile
    sh 'echo "{}" >> ' + auditFile
}

def injectFedmsgVars() {

    // Parse the CI_MESSAGE into a Map
    def ci_data = new JsonSlurper().parseText(env.CI_MESSAGE)

    // If we have a 'commit' key in the CI_MESSAGE, for each key under 'commit', we
    // * prepend the key name with fed_
    // * replace any '-' with '_'
    // * truncate the value for the key at the first '\n' character
    // * replace any double-quote characters with single-quote characters in the value for the key.

    if (ci_data['commit']) {
        ci_data.commit.each { key, value ->
            env."fed_${key.toString().replaceAll('-', '_')}" =
                    value.toString().split('\n')[0].replaceAll('"', '\'')
        }
        if (env.fed_branch == 'master'){
            env.branch = 'rawhide'
        } else {
            env.branch = env.fed_branch
        }
    }
}

/**
 * Library to prepare credentials
 * @return
 */
def prepareCredentials() {
    withCredentials([file(credentialsId: 'fedora-keytab', variable: 'FEDORA_KEYTAB')]) {
        sh '''
            #!/bin/bash
            set -xeuo pipefail
    
            cp ${FEDORA_KEYTAB} fedora.keytab
            chmod 0600 fedora.keytab
            
            mkdir -p ~/.ssh

            echo "Host *.ci.centos.org" > ~/.ssh/config
            echo "    StrictHostKeyChecking no" >> ~/.ssh/config
            echo "    UserKnownHostsFile /dev/null" >> ~/.ssh/config
            chmod 600 ~/.ssh/config
        '''
    }
    // Initialize RSYNC_PASSWORD from credentialsId
    env.RSYNC_PASSWORD = 'GOOD_RSYNC_PASSWORD'
}

def setDefaultEnvVars(Map envMap=null){

    // Check if we're working with a staging or production instance by
    // evaluating if env.ghprbActual is null, and if it's not, whether
    // it is something other than 'master'
    // If we're working with a staging instance:
    //      We default to an MAIN_TOPIC of 'org.centos.stage'
    // If we're working with a production instance:
    //      We default to an MAIN_TOPIC of 'org.centos.prod'
    // Regardless of whether we're working with staging or production,
    // if we're provided a value for MAIN_TOPIC in the build parameters:

    // We also set dataGrepperUrl which is needed for message tracking
    // and the correct jms-messaging message provider

    if (env.ghprbActualCommit != null && env.ghprbActualCommit != "master") {
        env.MAIN_TOPIC = env.MAIN_TOPIC ?: 'org.centos.stage'
        env.dataGrepperUrl = 'https://apps.stg.fedoraproject.org/datagrepper'
        env.MSG_PROVIDER = "fedora-fedmsg-stage"
    } else {
        env.MAIN_TOPIC = env.MAIN_TOPIC ?: 'org.centos.prod'
        env.dataGrepperUrl = 'https://apps.fedoraproject.org/datagrepper'
        env.MSG_PROVIDER = "fedora-fedmsg"
    }

    // Set our base HTTP_SERVER value
    env.HTTP_SERVER = env.HTTP_SERVER ?: 'http://artifacts.ci.centos.org'

    // Set our base RSYNC_SERVER value
    env.RSYNC_SERVER = env.RSYNC_SERVER ?: 'artifacts.ci.centos.org'
    env.RSYNC_USER = env.RSYNC_USER ?: 'fedora-atomic'

    // Check if we're working with a staging or production instance by
    // evaluating if env.ghprbActual is null, and if it's not, whether
    // it is something other than 'master'
    // If we're working with a staging instance:
    //      We default to an RSYNC_DIR of fedora-atomic/staging
    //      We default to an HTTP_DIR of fedora-atomic/staging
    // If we're working with a production instance:
    //      We default to an RSYNC_DIR of fedora-atomic
    //      We default to an HTTP_DIR of fedora-atomic
    // Regardless of whether we're working with staging or production,
    // if we're provided a value for RSYNC_DIR or HTTP_DIR in the build parameters:
    //      We set the RSYNC_DIR or HTTP_DIR to the value(s) provided (this overwrites staging or production paths)

    if (env.ghprbActualCommit != null && env.ghprbActualCommit != "master") {
        env.RSYNC_DIR = env.RSYNC_DIR ?: 'fedora-atomic/staging'
        env.HTTP_DIR = env.HTTP_DIR ?: 'fedora-atomic/staging'
    } else {
        env.RSYNC_DIR = env.RSYNC_DIR ?: 'fedora-atomic'
        env.HTTP_DIR = env.HTTP_DIR ?: 'fedora-atomic'
    }

    // Set env.HTTP_BASE to our env.HTTP_SERVER/HTTP_DIR,
    //  ex: http://artifacts.ci.centos.org/fedora-atomic/ (production)
    //  ex: http://artifacts.ci.centos.org/fedora-atomic/staging (staging)
    env.HTTP_BASE = "${env.HTTP_SERVER}/${env.HTTP_DIR}"

    env.basearch = env.basearch ?: 'x86_64'
    env.OSTREE_BRANCH = env.OSTREE_BRANCH ?: ''
    env.commit = env.commit ?: ''
    env.image2boot = env.image2boot ?: ''
    env.image_name = env.image_name ?: ''
    env.FEDORA_PRINCIPAL = env.FEDORA_PRINCIPAL ?: 'bpeck/jenkins-continuous-infra.apps.ci.centos.org@FEDORAPROJECT.ORG'
    env.package_url = env.package_url ?: ''
    env.nvr = env.nvr ?: ''
    env.original_spec_nvr = env.original_spec_nvr ?: ''
    env.ANSIBLE_HOST_KEY_CHECKING = env.ANSIBLE_HOST_KEY_CHECKING ?: 'False'

    // If we've been provided an envMap, we set env.key = value
    // Note: This may overwrite above specified values.
    envMap.each { key, value ->
        env."${key.toSTring().trim()}" = value.toString().trim()
    }
}

/**
 * Update the Build displayName and Description based on whether it
 * is a PR or a prod run.
 * Used at start of pipeline to decorate the build with info
 */
def updateBuildDisplayAndDescription() {
    currentBuild.displayName = "Build#: ${env.BUILD_NUMBER} - Branch: ${env.branch} - Package: ${env.fed_repo}"
    if (env.ghprbActualCommit != null && env.ghprbActualCommit != "master") {
        currentBuild.description = "<a href=\"https://github.com/${env.ghprbGhRepository}/pull/${env.ghprbPullId}\">PR #${env.ghprbPullId} (${env.ghprbPullAuthorLogin})</a>"
    }
}

/**
 *
 * @param openshiftProject name of openshift namespace/project.
 * @param nodeName podName we are going to verify.
 * @return
 */
def verifyPod(String openshiftProject, String nodeName) {
    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            def describeStr = openshift.selector("pods", nodeName).describe()
            out = describeStr.out.trim()
            writeFile file: 'node-pod-description-' + nodeName + '.txt',
                    text: out
            archiveArtifacts 'node-pod-description-' + nodeName + '.txt'

            timeout(60) {
                echo "Ensuring all containers are running in pod: ${env.NODE_NAME}"
                echo "Container names in pod ${env.NODE_NAME}: "
                def names       = openshift.raw("get", "pod",  "${env.NODE_NAME}", '-o=jsonpath="{.status.containerStatuses[*].name}"')
                echo names.out.trim()

                waitUntil {
                    def readyStates = openshift.raw("get", "pod",  "${env.NODE_NAME}", '-o=jsonpath="{.status.containerStatuses[*].ready}"')

                    echo "Container statuses: "
                    echo readyStates.out.trim()
                    def anyNotReady = readyStates.out.trim().contains("false")
                    if (anyNotReady) {
                        echo "One or more containers not ready...see above message ^^"
                        return false
                    } else {
                        echo "All containers ready!"
                        return true
                    }
                }
            }
        }
    }
}
