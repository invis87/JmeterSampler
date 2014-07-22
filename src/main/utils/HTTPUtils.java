package main.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.jmeter.samplers.SampleResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class HTTPUtils {
    private static JsonParser jsonParser = new JsonParser();

    private static final String TRACK_METHOD = "server.track";

    public static String makeURL(String host, String deployToken, int userNumber) {
        return generateApiRequestURL(host, generateUrlParams(deployToken, userNumber));
    }

    private static Map<String, String> generateUrlParams(String token, int userNumber) {
        Map<String, String> deviceParams = DeviceUtils.generateDeviceParams(userNumber);
        deviceParams.put("method", TRACK_METHOD);
        deviceParams.put("timestamp", Long.toString(System.currentTimeMillis()));
        deviceParams.put("token", token);

        return deviceParams;
    }

    private static String generateApiRequestURL(String host, Map<String, String> params) {
        StringBuilder queryBuilder = new StringBuilder(host);
        queryBuilder.append("/api");

        boolean first = true;
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (param.getValue() != null) {
                if (first) {
                    queryBuilder.append("?");
                    first = false;
                } else {
                    queryBuilder.append("&");
                }
                try {
                    queryBuilder.append(param.getKey()).append("=").append(URLEncoder.encode(param.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return queryBuilder.toString();
    }

    //----- Sending request

    public static SampleResult sendApiRequest(String apiRequestURL, byte[] batch) {
        SampleResult sampleResult = new SampleResult();
        sampleResult.setRequestHeaders(apiRequestURL);
        sampleResult.sampleStart();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(apiRequestURL);
            connection = (HttpURLConnection) url.openConnection(); // have to cast connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Connection", "close");

            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(batch.length);
            OutputStream out = connection.getOutputStream();
            out.write(batch);
            out.close();

            // Execute HTTP Post Request
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            try {
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    result.append(inputLine);
                }
            } finally {
                input.close();
                connection.disconnect();
                sampleResult.sampleEnd(); // stop stopwatch
            }

            try {
                JsonObject responseJson = jsonParser.parse(result.toString()).getAsJsonObject();
                String status;
                String message;
                String responseCode;

                String errorMessage = getErrorMessage(responseJson);
                if (errorMessage == null) {
                    status = responseJson.get("response").getAsJsonObject().get("status").getAsString();
                    responseCode = "200";
                    message = "";
                } else {
                    status = "FAIL";
                    message = errorMessage;
                    responseCode = "500";
                }

                sampleResult.setSuccessful(status.compareTo("OK") == 0);
                sampleResult.setResponseCode(responseCode);
                sampleResult.setResponseMessage(message);
                sampleResult.setResponseData(responseJson.toString(), "UTF-8");
            } catch (JsonSyntaxException jsonError) {
                sampleResult.setSuccessful(false);
                sampleResult.setResponseCode("500");
                sampleResult.setResponseMessage("JsonSyntaxException: " + jsonError.toString());
            }
        } catch (Exception e) {
            sampleResult.sampleEnd(); // stop stopwatch
            sampleResult.setSuccessful(false);
            sampleResult.setResponseMessage("Exception: " + e);

            // get stack trace as a String to return as document main.data
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            sampleResult.setResponseData(stringWriter.toString(), "UTF-8");
            sampleResult.setDataType(SampleResult.TEXT);
            sampleResult.setResponseCode("500");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return sampleResult;
    }

    private static String getErrorMessage(JsonObject response) {
        try {
            return response.get("error").getAsJsonObject().get("message").getAsString();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
