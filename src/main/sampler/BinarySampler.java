package main.sampler;

import main.data.BatchGenerator;
import main.utils.HTTPUtils;
import main.utils.SerializationUtil;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import java.io.IOException;
import java.io.Serializable;

public class BinarySampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String USER_NUMBER_PREFIX = "userNumber";

    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("host", "${__P(host)}");
        defaultParameters.addArgument("deployToken", "${__P(token)}");
        defaultParameters.addArgument("eventsFilePath", "${__P{eventsFilePath)}");
        defaultParameters.addArgument("userNumberAddiction", "${__P(userNumberAddiction)}");
        defaultParameters.addArgument("threadId", "${__threadNum}");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        int userNumber = context.getIntParameter("threadId") - 1 + context.getIntParameter("userNumberAddiction");
        String deployToken = context.getParameter("deployToken");
        String host = context.getParameter("host");
        String eventsFilePath = context.getParameter("eventsFilePath");

        String apiRequestURL = HTTPUtils.makeURL(host, deployToken, userNumber);
        String batchStr;
        try {
            batchStr = BatchGenerator.generateRandomBatch(getUserBatchId(userNumber), eventsFilePath);
        } catch (IOException ioe) {
            SampleResult sampleResult = new SampleResult();
            sampleResult.setSuccessful(false);
            sampleResult.setResponseCode("999");
            sampleResult.setResponseMessage("IOException! " + ioe.toString());

            return sampleResult;
        }

        byte[] batch = SerializationUtil.serializeStringGzip(batchStr);

        SampleResult sampleResult = HTTPUtils.sendApiRequest(apiRequestURL, batch);
        if (sampleResult.isSuccessful()) {
            incUserBatchId(userNumber);
        }

        return sampleResult;
    }

    private int getUserBatchId(int userNumber) {
        String lastBatch = JMeterUtils.getProperty(USER_NUMBER_PREFIX + userNumber);
        if (lastBatch != null) {
            return Integer.parseInt(lastBatch);
        } else {
            return 0;
        }
    }

    private void incUserBatchId(int userNumber) {
        String userNumberStr = USER_NUMBER_PREFIX + userNumber;
        String batchNumStr = JMeterUtils.getProperty(userNumberStr);

        if (batchNumStr != null) {
            int batchNum = Integer.parseInt(batchNumStr);
            JMeterUtils.setProperty(userNumberStr, Integer.toString(batchNum + 1));
        } else {
            JMeterUtils.setProperty(userNumberStr, "1");
        }
    }

}