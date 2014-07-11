package test;

import main.utils.HTTPUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UtilsTest {

    @Test
    public void HTTPUtilTest() {
        System.out.println(HTTPUtils.makeURL("localhost", "ad7af5d4-c863-4588-b05a-a3174c0e54a3", 34));
    }

    @Test
    public void lala(){
        Random rnd = new Random();

        long sum = 0;
        for(int i = 0; i< 1000 ; i++){
            sum += getNormallyDistributedEventsCount(rnd);
        }

        System.out.println(sum / 1000.);
    }

    private static long getNormallyDistributedEventsCount(Random rnd) {
        return Math.round(Math.abs(rnd.nextGaussian() * 3.5 + 7.5));
    }
}