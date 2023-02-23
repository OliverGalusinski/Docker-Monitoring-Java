package org.example;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        System.out.println("Hello world!");
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        dockerClient.pingCmd().exec();
        List<Container> containers = dockerClient.listContainersCmd().exec();

        for(Container container : containers){
            System.out.println(container.getId());
            LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(container.getId());
            logContainerCmd.withStdOut(true).withStdErr(true);
            // logContainerCmd.withTail(4);  // get only the last 4 log entries

            logContainerCmd.withTimestamps(true);

            try {
                logContainerCmd.exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(Frame item) {
                        System.out.println(item.toString());
                    }
                }).awaitCompletion();
            } catch (InterruptedException e) {
            }
        }
    }
}
