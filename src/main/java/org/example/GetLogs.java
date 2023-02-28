package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.LogContainerCmd;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
            ResultCallbackTemplate callbackTemplate = new ResultCallbackTemplate() {
                private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                @Override
                public void onNext(Object o) {
                    try{
                        this.dateFormat.parse(o.toString().split(" ")[1]);
                        logs.add(o.toString());
                    } catch (ParseException e){
                        logs.remove(logs.size()-1);
                        System.out.println("not valid");
                    }
                }
            };
            logContainerCmd.exec(callbackTemplate).awaitCompletion();
        } catch (Exception ie) {
            throw new RuntimeException(ie);
        }
        int unixTime = (int) Instant.now().getEpochSecond();
        return new GetLogsReturningValues(logs, unixTime);
    }
}
