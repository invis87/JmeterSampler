package main.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DeviceUtils {
    private static final char[] symbols = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final int tailCount = 8;
    private static final String IDFASource = "6788FF99-07B7-4AA3-BAA1-7977";
    private static final String AndroidIdSource = "8b1fcf71";

    private static final String ANDROID = "Android";
    private static final String IOS_UNIVERSAL = "iOSUniversal";

    private static final String MOB_MAC = "mobMac";
    private static final String MOB_OS_VERSION = "mobOSVer";
    private static final String OPEN_UDID = "mobOpenUDID";
    private static final String ANDROID_ID = "mobAndroidID"; //pure android ID
    private static final String IDFA = "mobAdvId";
    private static final String IDFA_ENABLED = "mobAdvIdEnabled";
    private static final String VENDOR_ID = "mobVendorId";

    public static Map<String, String> generateDeviceParams(int userNumber) {
        Random rnd = new Random();
        java.util.Map<java.lang.String, java.lang.String> params = new HashMap<String, String>();

        params.put(MOB_MAC, generateCharSequence(rnd, 12).toUpperCase());

        java.lang.String deviceType = generateDeviceType(rnd);
        if (ANDROID.equals(deviceType)) {

            params.put(MOB_OS_VERSION, "4.2.2");
            params.put(OPEN_UDID, generateCharSequence(rnd, 16));

            java.lang.String androidId = getUserAndroidId(userNumber);
            params.put(ANDROID_ID, androidId);
            params.put("userId", androidId);
        } else if (IOS_UNIVERSAL.equals(deviceType)) {

            params.put(MOB_OS_VERSION, "7.1");
            params.put(OPEN_UDID, generateCharSequence(rnd, 40));
            params.put(IDFA, getUserIDFA(userNumber));
            params.put(IDFA_ENABLED, "true");
            params.put(VENDOR_ID, UUID.randomUUID().toString().toUpperCase());
            params.put("userId", UUID.randomUUID().toString().toUpperCase());
        }

        return params;
    }

    private static String generateDeviceType(Random rnd) {
        //todo: for localhost debug, fix it!!!
        return ANDROID;
//    if(rnd.nextBoolean()){
//        return ANDROID;
//    } else {
//        return IOS_UNIVERSAL;
//    }
    }

    private static String getUserAndroidId(int userNumber) {
        return AndroidIdSource + getHexTail(userNumber);
    }

    private static String getUserIDFA(int userNumber) {
        return IDFASource + getHexTail(userNumber);
    }

    private static String getHexTail(int userNumber) {
        String hexStr = Integer.toHexString(userNumber);
        char[] zeros = new char[tailCount - hexStr.length()];
        for (int i = 0; i < zeros.length; i++) {
            zeros[i] = '0';
        }
        return new String(zeros) + hexStr;
    }


    private static String generateCharSequence(Random rnd, int length) {
        StringBuilder seq = new StringBuilder();

        for (int i = 0; i < length; i++) {
            seq.append(symbols[rnd.nextInt(symbols.length)]);
        }

        return seq.toString();
    }
}
