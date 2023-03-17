import com.github.dockerjava.api.model.Container;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonHandler {
    private final Container container;
    private final List<String> logs = new ArrayList<>();

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
        logs.add(log);
        String fileName ="/data/" +  container.getId() + ".json";
        try{
            FileWriter writer = new FileWriter(fileName);

            JSONParser jsonParser = new JSONParser();
            String toWrite = jsonParser.parse("{\"logs\": " +
                    logs.toString().replaceAll("\\[", "[\"")
                    .replaceAll("]", "\"]")
                    .replaceAll(",", "\", \"")  +
                    ", \"StorageUsed\": \"" + container.getSizeRootFs() +
                    "\" , \"Image\": \"" + container.getImage() + "\"}").toString();

            writer.write(toWrite);
            writer.flush();
            writer.close();
        } catch (IOException ignored){
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
