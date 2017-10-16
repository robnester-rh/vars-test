library identifier: "varsTest@master",
        retriever: modernSCM([$class: 'GitSCMSource',
                              remote: "https://github.com/robnester-rh/vars-test",
                              traits: [[$class: 'jenkins.plugins.git.traits.BranchDiscoveryTrait'],
                                       [$class: 'RefSpecsSCMSourceTrait',
                                        templates: [[value: '+refs/heads/*:refs/remotes/@{remote}/*'],
                                                    [value: '+refs/pull/*:refs/remotes/origin/pr/*']]]]])
String foo = """{"commit":{"username":"zdohnal","stats":{"files":{"README.patches":{"deletions":0,"additions":30,"lines":30},"sources":{"deletions":1,"additions":1,"lines":2},"vim.spec":{"deletions":7,"additions":19,"lines":26},".gitignore":{"deletions":0,"additions":1,"lines":1},"vim-8.0-rhbz1365258.patch":{"deletions":0,"additions":12,"lines":12}},"total":{"deletions":8,"files":5,"additions":63,"lines":71}},"name":"Zdenek Dohnal","rev":"3ff427e02625f810a2cedb754342be44d6161b39","namespace":"rpms","agent":"zdohnal","summary":"Merge branch 'f25' into f26","repo":"vim","branch":"f26","seen":false,"path":"/srv/git/repositories/rpms/vim.git","message":"Merge branch 'f25' into f26\n","email":"zdohnal@redhat.com"},"topic":"org.fedoraproject.prod.git.receive"}"""

pipeline{
    agent any
    parameters {
        string(defaultValue: foo,  description: 'CI Message that triggered the pipeline', name: 'CI_MESSAGE')
        string(defaultValue: 'f26', description: 'Fedora target branch', name: 'TARGET_BRANCH')
        string(defaultValue: '', description: 'HTTP Server', name: 'HTTP_SERVER')
        string(defaultValue: '', description: 'HTTP dir', name: 'HTTP_DIR')
        string(defaultValue: '', description: 'RSync User', name: 'RSYNC_USER')
        string(defaultValue: '', description: 'RSync Server', name: 'RSYNC_SERVER')
        string(defaultValue: '', description: 'RSync Dir', name: 'RSYNC_DIR')
        string(defaultValue: 'ci-pipeline', description: 'Main project repo', name: 'PROJECT_REPO')
        string(defaultValue: '', description: 'Main topic to publish on', name: 'MAIN_TOPIC')
        string(defaultValue: 'fedora-fedmsg', description: 'Main provider to send messages on', name: 'MSG_PROVIDER')
        string(defaultValue: '', description: 'Principal for authenticating with fedora build system', name: 'FEDORA_PRINCIPAL')
        string(defaultValue: 'master', description: '', name: 'ghprbActualCommit')
        string(defaultValue: 'CentOS-PaaS-SIG/ci-pipeline', description: '', name: 'ghprbGhRepository')
        string(defaultValue: '', description: '', name: 'sha1')
        string(defaultValue: '', description: '', name: 'ghprbPullId')
        string(defaultValue: '', description: '', name: 'ghprbPullAuthorLogin')
        string(defaultValue: 'stable', description: 'Tag for slave image', name: 'SLAVE_TAG')
        string(defaultValue: 'stable', description: 'Tag for rpmbuild image', name: 'RPMBUILD_TAG')
        string(defaultValue: 'stable', description: 'Tag for rsync image', name: 'RSYNC_TAG')
        string(defaultValue: 'stable', description: 'Tag for ostree-compose image', name: 'OSTREE_COMPOSE_TAG')
        string(defaultValue: 'stable', description: 'Tag for package test image', name: 'PACKAGE_TEST_TAG')
        string(defaultValue: '172.30.254.79:5000', description: 'Docker repo url for Openshift instance', name: 'DOCKER_REPO_URL')
        string(defaultValue: 'continuous-infra', description: 'Project namespace for Openshift operations', name: 'OPENSHIFT_NAMESPACE')
        string(defaultValue: 'jenkins', description: 'Service Account for Openshift operations', name: 'OPENSHIFT_SERVICE_ACCOUNT')
        booleanParam(defaultValue: false, description: 'Force generation of the image', name: 'GENERATE_IMAGE')
    }
    stages{
        stage('prep'){
            steps{
                prepareEnvironment('foo', 'bar', 'file')
                echo sh(returnStdout: true, script: 'env')
            }
        }
//        stage{'ci-pipeline-rpmbuild'}
//        stage{'ci-pipeline-ostree-compose'}
//        stage{'ci-pipeline-ostree-image-compose'}
//        stage{'ci-pipeline-ostree-image-boot-sanity'}
//        stage{'ci-pipeline-ostree-boot-sanity'}
//        stage{'ci-pipeline-functional-tests'}
//        stage{'ci-pipeline-atomic-host-tests'}
    }
//    post{
//
//    }
}