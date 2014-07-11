package main.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Random;

public class BatchGenerator {
    private static double eventsCountPerBatchDeviance = 3.5;
    private static double meanEventsCountPerBatch = 7.5;

    public static String generateRandomBatch(int userNumber) {
        int batchId = 5;//getUserLastBatchId(userNumber);//todo: fix, comment for compiling only
        Random rnd = new Random();

        JsonObject batchJson = new JsonObject();
        batchJson.add("batchId", new JsonPrimitive(batchId));

        JsonArray jsonEvents = new JsonArray();
        int eventsInBatch = (int) getNormallyDistributedEventsCount(rnd);
        if (batchId == 0) {
            jsonEvents.add(generateInstallEvent());
        }
        for(int i = 0; i < eventsInBatch; i++){
            jsonEvents.add(generateRandomEvent(rnd));
        }

        batchJson.add("batch", jsonEvents);
        return batchJson.toString();
    }

    private static JsonObject generateRandomEvent(Random rnd) {
        return new JsonObject();//todo: брать из тех тысяч набранных из Робинзона
    }

    private static JsonObject generateInstallEvent() {
        JsonObject installBatch = new JsonObject();
        installBatch.add("$country", new JsonPrimitive("RU"));
        installBatch.add("servertimestamp", new JsonPrimitive(System.currentTimeMillis()));
        installBatch.add("action", new JsonPrimitive("installBroadcast"));

        return installBatch;
    }

    private static long getNormallyDistributedEventsCount(Random rnd) {
        return Math.round(Math.abs(rnd.nextGaussian() * eventsCountPerBatchDeviance + meanEventsCountPerBatch));
    }
}
