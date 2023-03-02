import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Container;

//This should be responsible to Watch a single container.
//This means -> Getting its Metrics, for example Logs data and saving them in JSON Files
public class MonitorContainer extends Thread {
    private final int timeStamp;
    private final Container container;
    private final DockerClient dockerClient;
    private final JsonHandler jsonHandler;

    public MonitorContainer(DockerClient dockerClient, Container container){
        this.container = container;
        this.dockerClient = dockerClient;
        this.timeStamp = 0;
        this.jsonHandler = new JsonHandler(container);
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
            logContainerCmd.exec(callbackTemplate).awaitCompletion();
        } catch (Exception ie) {
            throw new RuntimeException(ie);
        }
    }

    public void stopThread(){
        System.out.println("Stopping Container Monitoring of " + this.container.getId());
        this.interrupt();
    }
}
