package test;

import main.utils.HTTPUtils;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UtilsTest {

    @Test
    public void HTTPUtilTest() {
        System.out.println(HTTPUtils.makeURL("localhost", "ad7af5d4-c863-4588-b05a-a3174c0e54a3", 34));
    }

    @Test
    public void distributionTest() {
        Random rnd = new Random();

        int eventsCount = 25000;
        long sum = 0;
        for (int i = 0; i < eventsCount; i++) {
            sum += getNormallyDistributedEventsCount(rnd);
        }

        System.out.println(sum / (double) eventsCount);
    }

    private static long getNormallyDistributedEventsCount(Random rnd) {
        return Math.round(Math.abs(rnd.nextGaussian() * 3.5 + 7.5)) + 1;
    }

    @Test
    public void getRandomLineNumbers() {
        Random rnd = new Random();
        int eventsInFile = 20;


        for (int j = 0; j < 40; j++) {

            int eventsInBatch = (int) getNormallyDistributedEventsCount(rnd);

            ArrayList<Integer> lineNumbersForBatch = new ArrayList<Integer>(eventsInBatch);
            for (int i = 0; i < eventsInBatch; i++) {
                int nextInt = rnd.nextInt(eventsInFile);
                if (!lineNumbersForBatch.contains(nextInt)) {
                    lineNumbersForBatch.add(nextInt);
                } else {
                    i--;
                }
            }
            System.out.println(lineNumbersForBatch.size());
        }
    }

    @Test
    public void saveAndLoadHashMap() throws IOException, ClassNotFoundException {
//        saveIntegerHashMap("temp");

        HashMap<Integer, Integer> lastBatchIdsMap = loadIntegerHashMap("/Users/pronvis/works/jmeter/performance/result/batchIdsMap");

        for (Map.Entry entry : lastBatchIdsMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    private void saveIntegerHashMap(String filePath) throws IOException{
        HashMap<Integer, Integer> fileObj = new HashMap<Integer, Integer>();

        fileObj.put(0, 25);
        fileObj.put(1, 2);
        fileObj.put(2, 5);
        fileObj.put(3, 20);
        fileObj.put(4, 1);

        File file = new File(filePath);
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(fileObj);
        s.close();
    }

    private HashMap<Integer, Integer> loadIntegerHashMap(String filePath) throws IOException{
        File file = new File(filePath);

        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        Object readedObject;
        try{
            readedObject = s.readObject();
        } catch (ClassNotFoundException e){
            readedObject = null;
        }
        s.close();

        if (readedObject == null) {
            readedObject = new HashMap<Integer, Integer>();
        }

        return (HashMap<Integer, Integer>) readedObject;
    }
}