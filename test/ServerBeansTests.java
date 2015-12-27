import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rlcp.calculate.RlcpCalculateRequest;
import rlcp.calculate.RlcpCalculateRequestBody;
import rlcp.calculate.RlcpCalculateResponse;
import rlcp.calculate.RlcpCalculateResponseBody;
import rlcp.check.RlcpCheckRequest;
import rlcp.check.RlcpCheckRequestBody;
import rlcp.check.RlcpCheckResponse;
import rlcp.check.RlcpCheckResponseBody;
import rlcp.exception.BadRlcpRequestException;
import rlcp.exception.BadRlcpResponseException;
import rlcp.generate.RlcpGenerateRequest;
import rlcp.generate.RlcpGenerateRequestBody;
import rlcp.generate.RlcpGenerateResponse;
import rlcp.generate.RlcpGenerateResponseBody;
import rlcp.server.Server;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-java-server-config.xml")
public class ServerBeansTests {

    @Autowired
    private Server server;
    @Autowired
    private String url;
    @Resource
    private HashMap<RlcpGenerateRequestBody, RlcpGenerateResponseBody> generateTest;
    @Resource
    private HashMap<RlcpCheckRequestBody, RlcpCheckResponseBody> checkTest;
    @Resource
    private HashMap<RlcpCalculateRequestBody, RlcpCalculateResponseBody> calculateTest;


    private Thread serverThread = new Thread() {
        @Override
        public void run() {
            server.startServer();
        }
    };

    @Before
    public void startServer() {
        serverThread.start();
    }

    @After
    public void closeServer() {
        serverThread.interrupt();
    }

    @Test
    public void testGenerateLogic() {
        generateTest.forEach(
                (generateRequest, generateResponse) -> {
                    RlcpGenerateRequest request = generateRequest.prepareRequest(url);
                    try {
                        RlcpGenerateResponse actualResponse = request.execute();

                        assertThat(actualResponse.toString(), is(not(equalTo(""))));
                        assertEquals(actualResponse.getMethod().getName(), generateResponse.getMethod().getName());

                        assertEquals(actualResponse.getBody().getText(), generateResponse.getText());
                        assertEquals(actualResponse.getBody().getCode(), generateResponse.getCode());
                        assertEquals(actualResponse.getBody().getInstructions(), generateResponse.getInstructions());

                    } catch (IOException | BadRlcpResponseException | BadRlcpRequestException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    public void testCheckLogic() {
        checkTest.forEach(
                (checkRequest, checkResponse) -> {
                    RlcpCheckRequest request = checkRequest.prepareRequest(url);
                    try {
                        RlcpCheckResponse actualResponse = request.execute();

                        assertThat(actualResponse.toString(), is(not(equalTo(""))));
                        assertEquals(actualResponse.getMethod().getName(), checkResponse.getMethod().getName());

                        actualResponse.getBody().getResults().forEach(
                                (result) -> {
                                    int id = result.getId();
//                                    assertEquals(result.getTime(), checkResponse.getResultById(id).getTime());
                                    assertEquals(result.getOutput(), checkResponse.getResultById(id).getOutput());
                                    assertEquals(result.getResult(), checkResponse.getResultById(id).getResult());
                                }
                        );

                    } catch (IOException | BadRlcpResponseException | BadRlcpRequestException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    public void testCalculate() {
        calculateTest.forEach(
                (calculateRequest, calculateResponse) -> {
                    RlcpCalculateRequest request = calculateRequest.prepareRequest(url);
                    try {
                        RlcpCalculateResponse actualResponse = request.execute();

                        assertThat(actualResponse.toString(), is(not(equalTo(""))));
                        assertEquals(actualResponse.getMethod().getName(), calculateResponse.getMethod().getName());

                        assertEquals(actualResponse.getBody().getText(), calculateResponse.getText());
                        assertEquals(actualResponse.getBody().getCode(), calculateResponse.getCode());
                    } catch (IOException | BadRlcpResponseException | BadRlcpRequestException e) {
                        e.printStackTrace();
                    }
                });
    }

}