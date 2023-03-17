import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.EventsCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

// This should constantly run and check for new Container to add them to a list
// Should also check if existing ones closed themselves, if so should safely close its thread
// Maintains all Information inside of Lists
public class CheckForNewContainer extends Thread{
    private final HashMap<String, MonitorContainer> monitoredContainers = new HashMap<>(); // A list of all Containers that are being Monitored
    private final DockerClient dockerClient;

    // Creates a Docker-client
    // Also check for all Available Containers
    public CheckForNewContainer() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    // This gets executed as Thread is being started
    public void run(){
        firstContainerCheck();
        try {
            EventsCmd eventsCmd = dockerClient.eventsCmd();

            eventsCmd.withEventTypeFilter("container")
                    .exec(new ResultCallbackTemplate<>() {
                        @Override
                        public void onNext(Event event) {
                            String eventAction = event.getAction();
                            if(eventAction.equals("start")) {
                                addContainerWithID(event.getId());
                            } else if(eventAction.equals("kill") || eventAction.equals("die") || eventAction.equals("destroy") || eventAction.equals("stop")){
                                stopContainer(event.getId());
                            }
                        }
                    }).awaitCompletion();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void firstContainerCheck(){
        dockerClient.listContainersCmd().exec().forEach(this::addContainer);
    }

    public void addContainerWithID(String containerID){
        List<Container> containers = dockerClient.listContainersCmd().exec();
        for (Container container : containers){
            if(container.getId().equals(containerID)){
                addContainer(container);
                break;
            }
        }
    }

    public void addContainer(Container toAdd){
        MonitorContainer monitorContainer = new MonitorContainer(this.dockerClient, toAdd);
        monitorContainer.setName(toAdd.getId());
        monitorContainer.start();
        monitoredContainers.put(toAdd.getId(), monitorContainer);
    }

    public void stopContainer(String containerID){
        System.out.println("Stopping Container Monitoring of " + containerID);
        monitoredContainers.remove(containerID);
    }
}
