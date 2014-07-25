package main.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.jmeter.util.JMeterUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class BatchGenerator {
    private static double eventsCountPerBatchDeviance = 3.5;
    private static double meanEventsCountPerBatch = 7.5;

    private static final String TIMESTAMP = "timestamp";
    private static final String SERVER_TIMESTAMP = "serverTimestamp";

    private static final String EVENT_NUMBER_PREFIX = "eventNumber";
    private static final String EVENTS_COUNT_PROPERTY_NAME = "eventsCountProperty";

    private static JsonParser jsonParser = new JsonParser();

    public static String generateRandomBatch(int userBatchId) {
        Random rnd = new Random();

        JsonObject batchJson = new JsonObject();
        batchJson.add("batchId", new JsonPrimitive(userBatchId));

        JsonArray jsonEvents = new JsonArray();
        int eventsInBatch = (int) getNormallyDistributedEventsCount(rnd);
        if (userBatchId == 0) {
            jsonEvents.add(generateInstallEvent());
        }

        int eventsInDBFile = Integer.parseInt(JMeterUtils.getProperty(EVENTS_COUNT_PROPERTY_NAME));

        ArrayList<Integer> lineNumbersForBatch = new ArrayList<Integer>(eventsInBatch);
        for (int i = 0; i < eventsInBatch; i++) {
            int nextInt = rnd.nextInt(eventsInDBFile);
            if (!lineNumbersForBatch.contains(nextInt)) {
                lineNumbersForBatch.add(nextInt);
            } else {
                i--;
            }
        }

        Collections.sort(lineNumbersForBatch);
        ArrayList<JsonObject> eventsFromFile = jsonEventsForLineNumbersFromProperties(lineNumbersForBatch);
        for (JsonObject event : eventsFromFile) {
            jsonEvents.add(event);
        }

        batchJson.add("batch", jsonEvents);
        return batchJson.toString();
    }

    private static JsonObject generateInstallEvent() {
        JsonObject installBatch = new JsonObject();
        installBatch.add("$country", new JsonPrimitive("RU"));
        JsonPrimitive currentTimestamp = new JsonPrimitive(System.currentTimeMillis());
        installBatch.add("servertimestamp", currentTimestamp);
        installBatch.add(TIMESTAMP, currentTimestamp);
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

    private static ArrayList<JsonObject> jsonEventsForLineNumbersFromFile(File file, ArrayList<Integer> lineNumbersForBatch) throws IOException {
        int eventsCount = lineNumbersForBatch.size();

        int lineNumberIndex = 0;
        int eventLineNumber = lineNumbersForBatch.get(lineNumberIndex);
        int fileLineNumber = 0;
        ArrayList<JsonObject> eventList = new ArrayList<JsonObject>(eventsCount);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            if (fileLineNumber == eventLineNumber) {
                eventList.add(changeEventFromFile(line));

                lineNumberIndex++;
                if (lineNumberIndex == lineNumbersForBatch.size()) {
                    break;
                }
                eventLineNumber = lineNumbersForBatch.get(lineNumberIndex);
            }
            fileLineNumber++;
        }

        br.close();

        return eventList;
    }

    private static ArrayList<JsonObject> jsonEventsForLineNumbersFromProperties(ArrayList<Integer> lineNumbersForBatch) {
        int eventsCount = lineNumbersForBatch.size();
        ArrayList<JsonObject> eventList = new ArrayList<JsonObject>(eventsCount);

        for (int lineNumber : lineNumbersForBatch) {
            eventList.add(changeEventFromFile(JMeterUtils.getProperty(EVENT_NUMBER_PREFIX + lineNumber)));
        }

        return eventList;
    }

    private static JsonObject changeEventFromFile(String jsonString) {
        JsonObject jsonEvent = jsonParser.parse(jsonString).getAsJsonObject();
        jsonEvent.remove(TIMESTAMP);
        jsonEvent.remove(SERVER_TIMESTAMP);
        jsonEvent.remove("__PIX__DEBUG__USER_BATCH_ID__");
        jsonEvent.remove("__PIX__DEBUG__BATCH_ID__");

        jsonEvent.add(TIMESTAMP, new JsonPrimitive(System.currentTimeMillis()));
        return jsonEvent;
    }

    private static long getNormallyDistributedEventsCount(Random rnd) {
        return Math.round(Math.abs(rnd.nextGaussian() * eventsCountPerBatchDeviance + meanEventsCountPerBatch)) + 1;
    }
}
