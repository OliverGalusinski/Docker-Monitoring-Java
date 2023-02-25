package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GetLogs {
    private GetLogs(){}

    public static GetLogsReturningValues getLogs(DockerClient dockerClient, String containerID, int timeStamp){
        List<String> logs = new ArrayList<>();

        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerID)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(true);

        if(timeStamp != 0){
            logContainerCmd.withSince(timeStamp);
        }

        try {
            logContainerCmd.exec(new LogContainerResultCallback() {
                @Override
                public void onNext(Frame item) {
                    logs.add(item.toString());
                }
            }).awaitCompletion();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
        int unixTime = (int) Instant.now().getEpochSecond();
        return new GetLogsReturningValues(logs, unixTime);
    }
}
