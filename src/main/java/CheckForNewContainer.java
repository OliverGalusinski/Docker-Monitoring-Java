import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.EventsCmd;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// This should constantly run and check for new Container to add them to a list
// Should also check if existing ones closed themselves, if so should safely close its thread
// Maintains all Information inside of Lists
public class CheckForNewContainer extends Thread{
    private final HashMap<String, MonitorContainer> monitoredContainers = new HashMap<>(); // A list of all Containers that are being Monitored
    private final ArrayList<String> containers = new ArrayList<>();
    private final DockerClient dockerClient;

    // Creates a Docker-client
    // Also check for all Available Containers
    public CheckForNewContainer(){
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
        checkForNewContainers();
        try {
            EventsCmd eventsCmd = dockerClient.eventsCmd();

            eventsCmd.withEventTypeFilter("container")
                    .withEventFilter("create")
                    .exec(new ResultCallbackTemplate<>() {
                        @Override
                        public void onNext(Event event) {
                            checkForNewContainers();
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkForNewContainers(){
        List<Container> newContainers = dockerClient.listContainersCmd().withShowSize(true).withShowAll(true).exec(); // Creates a list of all new Containers

        // If newContainer doesn't exist
        newContainers.forEach(newContainer -> {
            if(!containers.contains(newContainer.getId())){
                MonitorContainer monitorContainer = new MonitorContainer(dockerClient, newContainer);
                monitorContainer.start();
                containers.add(newContainer.getId());
                monitoredContainers.put(newContainer.getId(), monitorContainer);
                System.out.println("Added new Container: " + newContainer.getId());
            }
        });

        // If newContainer doesn't exist
        for(int i = 0; i < containers.size(); i++){
            int finalI = i;
            if(newContainers.stream().noneMatch(newContainer -> newContainer.getId().equals(containers.get(finalI)))){
                monitoredContainers.get(containers.get(i)).stopThread();
                monitoredContainers.remove(containers.get(i));
                containers.remove(i);
                i--;
            }
        }
    }
}
