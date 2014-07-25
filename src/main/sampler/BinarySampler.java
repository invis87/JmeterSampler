package main.sampler;

import main.data.BatchGenerator;
import main.utils.HTTPUtils;
import main.utils.SerializationUtil;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Logger;

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
        defaultParameters.addArgument("userNumberAddiction", "${__P(userAddiction)}");
        defaultParameters.addArgument("threadId", "${__threadNum}");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        Logger logger = getLogger();
        int threadNumber = context.getIntParameter("threadId");
        int userNumber = threadNumber - 1 + context.getIntParameter("userNumberAddiction");
        String deployToken = context.getParameter("deployToken");
        String host = context.getParameter("host");

        String apiRequestURL = HTTPUtils.makeURL(host, deployToken, userNumber);
        String batchStr = BatchGenerator.generateRandomBatch(getUserBatchId(userNumber));

        byte[] batch = SerializationUtil.serializeStringGzip(batchStr);

        SampleResult sampleResult = HTTPUtils.sendApiRequest(apiRequestURL, batch);
        if (sampleResult.isSuccessful()) {
            incUserBatchId(userNumber);
        }

        if(!sampleResult.isSuccessful()){
            logger.error("Thread Number - " + threadNumber + "\nResponse message = " + sampleResult.getResponseMessage() + "; URL = " + apiRequestURL);
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