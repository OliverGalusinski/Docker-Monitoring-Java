package org.example;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.List;

public class JsonHandler {
    // Creates and Handles JSON Files.
    // Also Implements HTTP Send request to the Server.
    private final String containerID;

    public JsonHandler(String containerID){
        this.containerID = containerID;
        this.createJson(containerID);
    }

    public void createJson(String filePath){
        try{
            File file = new File(filePath + ".json");
            if (!file.exists()){
                file.createNewFile();
                System.out.println("Created");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean writeListIntoFile(List<String> toWrite) {
        String fileName = containerID + ".json";
        try{
            //Read File
            List<String> oldLogs = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals("{") && !line.equals("}")){
                    oldLogs.add(line.split("\"")[3]);
                }
            }
            br.close();

            toWrite.addAll(0, oldLogs);
            FileWriter writer = new FileWriter(fileName);

            writer.write(convertListToJsonString(toWrite));
            writer.flush();
            writer.close();
        } catch (IOException ioException){
            return false;
        }
        return true;
    }

    public String convertListToJsonString(List<String> toConvert){
        StringBuilder converted = new StringBuilder("{\n");
        toConvert.stream().forEach(log -> {
            String timeStamp = log.split(" ")[1];
            if(!log.equals(toConvert.get(toConvert.size()-1))){
                converted.append("\"" + timeStamp + "\": \"" + log +"\",\n");
            } else {
                converted.append("\"" + timeStamp + "\": \"" + log + "\"\n}");
            }
        });
        return converted.toString();
    }
}
