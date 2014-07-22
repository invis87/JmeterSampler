package main.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class BatchGenerator {
    private static double eventsCountPerBatchDeviance = 3.5;
    private static double meanEventsCountPerBatch = 7.5;

    public static String generateRandomBatch(int userNumber, String eventsFilePath) throws IOException {
        int batchId = 5;//getUserLastBatchId(userNumber);//todo: fix, comment for compiling only
        Random rnd = new Random();

        JsonObject batchJson = new JsonObject();
        batchJson.add("batchId", new JsonPrimitive(batchId));

        JsonArray jsonEvents = new JsonArray();
        int eventsInBatch = (int) getNormallyDistributedEventsCount(rnd);
        if (batchId == 0) {
            jsonEvents.add(generateInstallEvent());
        }

        File file = new File(eventsFilePath);
        if (file.canRead()) {
            int eventsInFile = getLinesNumberInFile(file);
            ArrayList<Integer> lineNumbersForBatch = new ArrayList<Integer>(eventsInBatch);
            //todo: заполняю массив чиселками рандомными, чтобы потом считывать строки с этими номерами из файла
            for (int i = 0; i < eventsInBatch; i++) {
                int nextInt = rnd.nextInt(eventsInFile);
                if (!lineNumbersForBatch.contains(nextInt)) {
                    lineNumbersForBatch.add(nextInt);
                } else {
                    i--;
                }
            }

            //todo: формирую массив джейсонов из текстового файла
            ArrayList<JsonObject> eventsFromFile = jsonEventsForLineNumbers(file, lineNumbersForBatch);
            for (JsonObject event : eventsFromFile) {
                jsonEvents.add(event);
            }

            batchJson.add("batch", jsonEvents);
            return batchJson.toString();
        } else {
            return null;
        }
    }

    private static JsonObject generateInstallEvent() {
        JsonObject installBatch = new JsonObject();
        installBatch.add("$country", new JsonPrimitive("RU"));
        installBatch.add("servertimestamp", new JsonPrimitive(System.currentTimeMillis()));
        installBatch.add("action", new JsonPrimitive("installBroadcast"));

        return installBatch;
    }

    private static int getLinesNumberInFile(File file) throws IOException {
        LineNumberReader lnr = new LineNumberReader(new FileReader(file));
        lnr.skip(Long.MAX_VALUE);
        int linesNumber = lnr.getLineNumber();
        lnr.close();

        return linesNumber;
    }

    private static ArrayList<JsonObject> jsonEventsForLineNumbers(File file, ArrayList<Integer> lineNumbersForBatch) throws IOException {
        int eventsCount = lineNumbersForBatch.size();

        int lineNumberIndex = 0;
        int eventLineNumber = lineNumbersForBatch.get(lineNumberIndex);
        int fileLineNumber = 0;
        ArrayList<JsonObject> eventList = new ArrayList<JsonObject>(eventsCount);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            if (lineNumberIndex == eventsCount) {
                break;
            }

            if (fileLineNumber == eventLineNumber) {
                eventList.add(changeEventFromFile(line));

                eventLineNumber = lineNumbersForBatch.get(lineNumberIndex);
                lineNumberIndex++;
            }
            fileLineNumber++;
        }

        br.close();

        return eventList;
    }

    private static JsonObject changeEventFromFile(String jsonString) {
        return new JsonParser().parse(jsonString).getAsJsonObject(); //todo: temporarily!!! dont create Gson() every time!!!
    }

    private static long getNormallyDistributedEventsCount(Random rnd) {
        return Math.round(Math.abs(rnd.nextGaussian() * eventsCountPerBatchDeviance + meanEventsCountPerBatch));
    }
}
