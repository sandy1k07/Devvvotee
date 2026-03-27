package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.deploy.DeploymentResponse;
import com.springboot.projects.devvvotee.Service.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeploymentServiceImpl implements DeploymentService {

    private final KubernetesClient kubernetesClient;

    private static final String NAMESPACE = "java-projects";
    private static final String POOL_LABEL = "status";
    private static final String PROJECT_LABEL = "project_id";
    private static final String IDLE = "idle";
    private static final String BUSY = "busy";
    private static final String SYNCER_CONTAINER = "syncer";
    private static final String RUNNER_CONTAINER = "runner";
    private static final String REVERSE_PROXY_PORT = "8090";

    @Override
    public DeploymentResponse deploy(Long projectId) {
        String domain = "project-" + projectId + ".app.domain.com";
        Pod existingPod = findActivePod(projectId);

        if(existingPod == null) {
            startNewPod(projectId, domain);
        }

        //            return new DeploymentResponse("http://localhost:5840");
        return new DeploymentResponse("http://" + domain + ":" + REVERSE_PROXY_PORT);
    }

    private void startNewPod(Long projectId, String domain) {
        Pod pod = kubernetesClient.pods().inNamespace(NAMESPACE)
                .withLabel(POOL_LABEL, IDLE)
                .list().getItems().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Pod not available, please scale up"));

        String podName = pod.getMetadata().getName();
        log.info("Pod {} claimed for projectid: {}", podName, projectId);

        kubernetesClient.pods().inNamespace(NAMESPACE)
                .withName(podName).editStatus(p -> {
                    p.getMetadata().getLabels().putAll(Map.of(POOL_LABEL, BUSY, PROJECT_LABEL, projectId.toString()));
                    return p;
                });

        try{
// SYNCER CMDS
            String initialSyncCmd = String.format("mc mirror --overwrite myminio/projects/project_%d/ /app/",
                    projectId); // for copying files from minio and overwrite in the containers volume if any

            log.info("Starting initial sync for project {} in pod {}", projectId, podName);
            executeCommand(podName, SYNCER_CONTAINER, "sh", "-c", initialSyncCmd);

            String watchCmd = String.format(
                    "nohup mc mirror --overwrite --watch myminio/projects/project_%d/ /app/ > /app/sync.log 2>&1 &",
                    projectId); // no hangup flag added to keep on syncing the changes or overwriting again and again
            // last command is for writing the logs, 2>&1 means whatever you're doing with error logs, do with info logs as well
            // '&' at the end means to run forever
            executeCommand(podName, SYNCER_CONTAINER, "sh", "-c", watchCmd);



            // RUNNER CMDS
            String startCmd = "npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 &";
            // 0.0.0.0 asks the vite server to listen to all the network interfaces that it has, not just localhost

            log.info("Starting dev server for project {}...", projectId);
            executeCommand(podName, RUNNER_CONTAINER, "sh", "-c", startCmd);


            log.info("Deployment successful: http://{}:{}", domain, REVERSE_PROXY_PORT);
        } catch (Exception e) {
            log.info("Deployment failed: http://{}:{}", domain, REVERSE_PROXY_PORT);
            kubernetesClient.pods().inNamespace(NAMESPACE).withName(podName).delete();
            throw new RuntimeException("Failed to deploy project " + projectId);
        }
    }

    private void executeCommand(String podName, String container, String ...command){
        log.debug("Exec in {}:{} -> {}", podName, container, String.join(" ", command));

        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch ignored = kubernetesClient.pods().inNamespace(NAMESPACE).withName(podName)
                .inContainer(container)
                .writingOutput(new ByteArrayOutputStream())
                .writingError(new ByteArrayOutputStream())
                .usingListener(new ExecListener(){
                    @Override
                    public void onClose(int code, String reason) {
                        data.complete("Done");
                    }
                })
                .exec(command)) {

            // Wait briefly to ensure command fired (Fabric8 exec is async)
            // For long running background jobs (nohup), we don't wait for "Done"
            if (command[command.length - 1].trim().endsWith("&")) {
                Thread.sleep(500);
            } else {
                data.get(30, TimeUnit.SECONDS); // Block for synchronous setup commands (npm install)
            }

        } catch (Exception e) {
            log.error("Exec failed", e);
            throw new RuntimeException("Pod Execution Failed", e);
        }
    }

    private Pod findActivePod(Long projectId) {
        return kubernetesClient.pods().inNamespace(NAMESPACE)
                .withLabels(Map.of(PROJECT_LABEL, projectId.toString(), POOL_LABEL, BUSY))
                .list().getItems().stream()
                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                .findFirst()
                .orElse(null);
    }
}
