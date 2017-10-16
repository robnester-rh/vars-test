import com.robnester.ci.utils.Utils

def call(String openshiftNamespace, String nodeName, String msgAuditFile) {
    def pipelineUtils = new Utils()

    deleteDir()
    pipelineUtils.setDefaultEnvVars()
    pipelineUtils.prepareCredentials()
    pipelineUtils.injectFedmsgVars()
    pipelineUtils.updateBuildDisplayAndDescription()
    pipelineUtils.verifyPod(openshiftNamespace, nodeName)
    pipelineUtils.initializeAuditFile(msgAuditFile)
}