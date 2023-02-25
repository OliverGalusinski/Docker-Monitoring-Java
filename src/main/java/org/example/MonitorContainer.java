package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import java.util.Map;

//This should be responsable to Watch a single container.
//This means -> Getting its Metrics, for example Logs data and saving them in JSON Files
public class MonitorContainer extends Thread {
    private int timeStamp;
    private Container container;
    private DockerClient dockerClient;
    public MonitorContainer(DockerClient dockerClient, Container container){
        this.container = container;
        this.dockerClient = dockerClient;
        this.timeStamp = 0;
    }

    public void run(){
        System.out.println("Monitoring " + this.container.getId());
        while(!isInterrupted()){
            System.out.println("Getting new Logs from " + container.getId());
            GetLogsReturningValues logsAndTimeStamp = GetLogs.getLogs(dockerClient, container.getId(), timeStamp);
            this.timeStamp = logsAndTimeStamp.timeStamp();
            logsAndTimeStamp.logs().forEach(log -> System.out.println(log));
        }
    }

    public void stopThread(){
        System.out.println("Stopping Container Monitoring of " + this.container.getId());
        this.interrupt();
    }
}
