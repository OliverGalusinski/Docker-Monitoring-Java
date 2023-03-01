import java.io.*;

public record JsonHandler(String containerID) {
    public JsonHandler(String containerID) {
        this.containerID = containerID;
        this.createJson(containerID);
    }

    public void createJson(String filePath) {
        try {
            File file = new File("savedData/" + filePath + ".json");
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("Created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean writeLogOntoFile(String log) {
        String fileName = containerID + ".json";
        StringBuilder toWrite = new StringBuilder();
        try{
            //Read File
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                toWrite.append(line + "\n");
            }
            br.close();

            toWrite.append(log + "\n");

            FileWriter writer = new FileWriter(fileName);

            writer.write(toWrite.toString());
            writer.flush();
            writer.close();
        } catch (IOException ioException){
            return false;
        }
        return true;
    }
}
