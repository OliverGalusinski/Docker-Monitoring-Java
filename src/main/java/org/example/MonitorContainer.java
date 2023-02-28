package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import java.io.IOException;

//This should be responsable to Watch a single container.
//This means -> Getting its Metrics, for example Logs data and saving them in JSON Files
public class MonitorContainer extends Thread {
    private int timeStamp;
    private final Container container;
    private final DockerClient dockerClient;
    private final JsonHandler jsonHandler;

    public MonitorContainer(DockerClient dockerClient, Container container){
        this.container = container;
        this.dockerClient = dockerClient;
        this.timeStamp = 0;
        this.jsonHandler = new JsonHandler(container.getId());
    }

    public void run(){
        System.out.println("Monitoring " + this.container.getId());
        while(!isInterrupted()){
            System.out.println("Getting new Logs from " + container.getId());
            GetLogsReturningValues logsAndTimeStamp = GetLogs.getLogs(dockerClient, container.getId(), timeStamp);
            if(!logsAndTimeStamp.logs().isEmpty()){
                this.timeStamp = logsAndTimeStamp.timeStamp();
                // logsAndTimeStamp.logs().forEach(System.out::println);
                jsonHandler.writeListIntoFile(logsAndTimeStamp.logs());
            }
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopThread(){
        System.out.println("Stopping Container Monitoring of " + this.container.getId());
        this.interrupt();
    }
}
