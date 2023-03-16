import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Statistics;

import java.io.Closeable;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//This should be responsible to Watch a single container.
//This means -> Getting its Metrics, for example Logs data and saving them in JSON Files
public class MonitorContainer extends Thread {
    private final int timeStamp;
    private final Container container;
    private final DockerClient dockerClient;
    private final JsonHandler jsonHandler;
    private final long totalMemory;
    private Info cpuInfo;

    public MonitorContainer(DockerClient dockerClient, Container container){
        this.container = container;
        this.dockerClient = dockerClient;
        this.timeStamp = 0;
        this.jsonHandler = new JsonHandler(container);

        this.totalMemory = dockerClient.infoCmd().exec().getMemTotal();
    }

    public void run(){
        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(container.getId())
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(true)
                .withFollowStream(true);

        if(timeStamp != 0){
            logContainerCmd.withSince(timeStamp);
        }

        try {
            ResultCallbackTemplate callbackTemplate = new ResultCallbackTemplate() {
                @Override
                public void onNext(Object o) {
                    try {
                        jsonHandler.writeLogOntoFile(o.toString());
                    } catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }
            };
            getStats();
            logContainerCmd.exec(callbackTemplate).awaitCompletion();
        } catch (Exception ie) {
            throw new RuntimeException(ie);
        }
    }

    public void stopThread(){
        System.out.println("Stopping Container Monitoring of " + this.container.getId());
        this.interrupt();
    }

    private void getStats() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        dockerClient.statsCmd(container.getId()).exec(new ResultCallback<Statistics>() {

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(Statistics statistics) {
                System.out.println("Container ID: " + container.getId());
                System.out.println("CPU usage: " + Objects.requireNonNull(statistics.getCpuStats().getCpuUsage()).getTotalUsage());
                System.out.println("von : " + statistics.getCpuStats().getSystemCpuUsage());
                System.out.println("Memory usage: " + statistics.getMemoryStats().getUsage());
                System.out.println("von : " + totalMemory);
                System.out.println("Network RX: " + statistics.getNetworks().get("eth0").getRxBytes());
                System.out.println("Network TX: " + statistics.getNetworks().get("eth0").getTxBytes());
                System.out.println("------------------------------------------");
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onComplete() {
                countDownLatch.countDown();
            }

            @Override
            public void close() throws IOException {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}
