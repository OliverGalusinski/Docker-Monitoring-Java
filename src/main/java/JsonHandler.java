import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonHandler {
    private Container container;
    private final List<String> logs = new ArrayList<>();
    private final List<String> containers = new ArrayList<>();
    private final List<String> statsList = new ArrayList<>();

    public JsonHandler(){
        this.createJson("containers");
    }

    public void addContainer(String containerId) {
        containers.add(containerId);
        try{
            JSONParser jsonParser = new JSONParser();
            FileWriter writer = new FileWriter("/data/containers.json");
            String toWrite = jsonParser.parse("{\"containers\": " +
                    containers.toString().replaceAll("\\[", "[\"")
                            .replaceAll("]", "\"]")
                            .replaceAll(",", "\", \"") + "}").toString();
            writer.write(toWrite);
            writer.flush();
            writer.close();
        } catch (IOException ignored){
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveStats(Statistics stats){
        long cpuTotalUsage = stats.getCpuStats().getCpuUsage().getTotalUsage();
        long systemCpuUsage = stats.getCpuStats().getSystemCpuUsage();
        long preCpuTotalUsage = stats.getPreCpuStats().getCpuUsage().getTotalUsage();
        long preSystemCpuUsage = 0;
        if(stats.getPreCpuStats().getSystemCpuUsage() != null){
            preSystemCpuUsage = stats.getPreCpuStats().getSystemCpuUsage();
        }


        long cpuDelta = cpuTotalUsage - preCpuTotalUsage;
        long systemDelta = systemCpuUsage - preSystemCpuUsage;

        int cpuPercent = 0;
        if (systemDelta > 0 && cpuDelta > 0) {
            cpuPercent = (int) (cpuDelta * 100 / systemDelta);
        }
        long memoryUsage = stats.getMemoryStats().getUsage();
        long memoryLimit = stats.getMemoryStats().getLimit();
        int memoryPercent = (int) (memoryUsage * 100 / memoryLimit);

        statsList.add("\"cpuTotalUsage\": \"" + cpuTotalUsage + "\"");
        statsList.add("\"systemCpuUsage\": \"" + systemCpuUsage + "\"");
        statsList.add("\"preCpuTotalUsage\": \"" + preCpuTotalUsage + "\"");
        statsList.add("\"preSystemCpuUsage\": \"" + preSystemCpuUsage + "\"");
        statsList.add("\"memoryUsage\": \"" + memoryUsage + "\"");
        statsList.add("\"memoryLimit\": \"" + memoryLimit + "\"");
        statsList.add("\"memoryPercent\": \"" + memoryPercent + "\"");
        statsList.add("\"cpuPercent\": \"" + cpuPercent + "\"");

        this.writeLogOntoFile("");
    }

    public JsonHandler(Container container) {
        this.container = container;
        this.createJson(container.getId());
    }

    public void createJson(String filePath) {
        try {
            File file = new File("/data/" + filePath + ".json");
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("Created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLogOntoFile(String log) {
        if(!log.equals("")){
            logs.add(log);
        }
        String fileName ="/data/" +  container.getId() + ".json";
        try{
            FileWriter writer = new FileWriter(fileName);

            JSONParser jsonParser = new JSONParser();
            String toWrite = "{\"logs\": " +
                    logs.toString().replaceAll("\\[", "[\"")
                    .replaceAll("]", "\"]")
                    .replaceAll(",", "\", \"")  +
                    ", \"StorageUsed\": \"" + container.getSizeRootFs() +
                    "\" , \"Image\": \"" + container.getImage() + "\", ";

            for(String stat : statsList){
                toWrite += ", " + stat;
            }
            toWrite = jsonParser.parse(toWrite + "}").toString();

            writer.write(toWrite);
            writer.flush();
            writer.close();
        } catch (IOException ie){
            System.out.println(ie);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
