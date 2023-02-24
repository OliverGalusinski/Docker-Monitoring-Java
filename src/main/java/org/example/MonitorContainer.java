package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import java.util.Map;

//This should be responsable to Watch a single container.
//This means -> Getting its Metrics, for example Logs data and saving them in JSON Files
public class MonitorContainer extends Thread {
    Container container;
    DockerClient dockerClient;
    public MonitorContainer(DockerClient dockerClient, Container container){
        this.container = container;
    }

    public void run(){
        System.out.println("Monitoring " + this.container.getId());
        while(!isInterrupted()){

        }

    }

    public void stopThread(){
        System.out.println("Stopping Container Monitoring of " + this.container.getId());
        this.interrupt();
    }
}
