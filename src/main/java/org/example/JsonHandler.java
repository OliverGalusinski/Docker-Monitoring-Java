package org.example;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonHandler {
    // Creates and Handles JSON Files.
    // Also Implements HTTP Send request to the Server.
    private String containerID;
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
            StringBuilder jsonString = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            JSONObject logData = new JSONObject();
            if(!jsonString.isEmpty()){
                JSONParser parser = new JSONParser();
                System.out.println(jsonString.toString());
                logData = (JSONObject) parser.parse(jsonString.toString());
            }



            FileWriter writer = new FileWriter(fileName);
            JSONObject finalLogData = logData;
            toWrite.forEach(singleLog -> {
                finalLogData.put(singleLog, 1);
            });

            writer.write(finalLogData.toJSONString());
            writer.flush();
            writer.close();
        } catch (IOException ioException){
            return false;
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}
