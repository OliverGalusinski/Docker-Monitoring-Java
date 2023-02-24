package org.example;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        /*
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        List<Container> containers = dockerClient.listContainersCmd().exec();

        for(Container container : containers){
            System.out.println("The container " + container.getNames()[0] + " with the ID " + container.getId() + " logged:");
            List<String> logs = GetLogs.getLogs(dockerClient, container.getId(), 1677166678);
            for(String log : logs){
                System.out.println(log);
            }
        }
         */

        CheckForNewContainer checkForNewContainer = new CheckForNewContainer();
        checkForNewContainer.setName("CheckForNewContainer-Thread");
        checkForNewContainer.start();
    }
}
