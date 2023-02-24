package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.ArrayList;
import java.util.List;

public class GetLogs {
    private GetLogs(){}

    public static List<String> getLogs(DockerClient dockerClient, String containerID, int timeStamp){
        List<String> logs = new ArrayList<>();

        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerID);
        logContainerCmd.withStdOut(true).withStdErr(true);
        logContainerCmd.withSince(1677166678);
        // logContainerCmd.withTail(4);  // get only the last 4 log entries

        logContainerCmd.withTimestamps(true);

        try {
            logContainerCmd.exec(new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    logs.add(item.toString());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
        }
        return logs;
    }
}
