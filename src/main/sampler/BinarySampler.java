package main.sampler;

import main.data.BatchGenerator;
import main.utils.HTTPUtils;
import main.utils.SerializationUtil;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

public class BinarySampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String perfomanceDeployToken = "62f92b95-99e7-4108-8670-4ee081f42cd8";
    private static final String localhostTestDeployToken = "4f683176-072f-4430-a0c9-e5aa6317ceb2";
    private static final String stageDeployToken = "8f3d861a-4064-46db-afa0-74c36938bf57";

    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("host", "stage.pixapi.net");
        defaultParameters.addArgument("deployToken", "8f3d861a-4064-46db-afa0-74c36938bf57");
        defaultParameters.addArgument("eventsFilePath", "/Users/pronvis/works/jmeter/timelines/timeline.txt");
        defaultParameters.addArgument("userNumberAddiction", "0");
        defaultParameters.addArgument("threadId", "${__threadNum}");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        // pull parameters
        int threadId = 0;
        try {
            threadId = Integer.parseInt(context.getParameter("threadId"));
        } catch (NumberFormatException nfe) {
            SampleResult sampleResult = new SampleResult();
            sampleResult.setSuccessful(false);
            sampleResult.setResponseCode("999");
            Iterator<String> iterator = context.getParameterNamesIterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                stringBuilder.append(iterator.next()).append(" ");
            }
            sampleResult.setResponseMessage("NumberFormatException! " + nfe.toString() + "\n" + "Have 'threadId' parameter = " + context.containsParameter("threadId") + "\n" + "All parameters = " + stringBuilder.toString());

            return sampleResult;
        }
        int userNumber = threadId - 1 + context.getIntParameter("userNumberAddiction");
        String deployToken = context.getParameter("deployToken");
        String host = context.getParameter("host");
        String eventsFilePath = context.getParameter("eventsFilePath");

        String apiRequestURL = HTTPUtils.makeURL(host, deployToken, userNumber);
        String batchStr;
        try {
            batchStr = BatchGenerator.generateRandomBatch(userNumber, eventsFilePath);
        } catch (IOException ioe) {
            SampleResult sampleResult = new SampleResult();
            sampleResult.setSuccessful(false);
            sampleResult.setResponseCode("999");
            sampleResult.setResponseMessage("IOException! " + ioe.toString());

            return sampleResult;
        }

        byte[] batch = SerializationUtil.serializeStringGzip(batchStr);

        return HTTPUtils.sendApiRequest(apiRequestURL, batch);
    }
}