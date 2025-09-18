package org.cibicom.iot.js;


import org.cibicom.iot.js.data.lrwan.backendif.JoinAns;
import org.cibicom.iot.js.data.lrwan.backendif.Result;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cibicom.iot.js.service.device.DevKeyIdService;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.log.JoinLogService;
import org.cibicom.iot.js.service.lrwan.JoinProcessor;
import org.cibicom.iot.js.service.utils.RunningJobService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class JoinReqParseTests {
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private KeyCredentialService keyCredentialService;
    @Autowired
    private DevKeyIdService devIdService;
    @Autowired
    private JoinLogService joinLogService;
    @Autowired
    private RunningJobService jobService;


    @Test
    public void testCorrectParsing() {
        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000B7448E450ADF\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService);

        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);

        Assert.isTrue(
                !joinAns.getResult().getResultCode().equals("MalformedMessage") &&
                        !joinAns.getResult().getDescription().contains(Result.jsonParsingFailedErr),
                "ResultCode should not be MalformedMessage and description should not contain " + Result.jsonParsingFailedErr
        );
    }

    @Test
    public void testSyntaxErr() {
        // Missing '{' in the beginning
        String joinReqJson = "\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000B7448E450ADF\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        // Missing senderID before ':'
        String joinReqJson1 = "{\"ProtocolVersion\":\"1.0\",:\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000B7448E450ADF\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        // Missing 'MACVersion:'
        String joinReqJson2 = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000B7448E450ADF\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqJson3 = "{\"ProtocolVersion\"\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000B7448E450ADF\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService);

        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);
        JoinAns joinAns1 = joinProcessor.processJoinReq(joinReqJson1);
        JoinAns joinAns2 = joinProcessor.processJoinReq(joinReqJson2);
        JoinAns joinAns3 = joinProcessor.processJoinReq(joinReqJson3);


        Assert.isTrue(
                joinAns.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAns.getResult().getDescription().contains(Result.jsonParsingFailedErr),
                "ResultCode should be MalformedMessage and description should contain '" + Result.jsonParsingFailedErr + "'"
        );
        Assert.isTrue(
                joinAns1.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAns1.getResult().getDescription().contains(Result.jsonParsingFailedErr),
                "ResultCode should be MalformedMessage and description should contain '" + Result.jsonParsingFailedErr + "'"
        );
        Assert.isTrue(
                joinAns2.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAns2.getResult().getDescription().contains(Result.jsonParsingFailedErr),
                "ResultCode should be MalformedMessage and description should contain '" + Result.jsonParsingFailedErr + "'"
        );
        Assert.isTrue(
                joinAns3.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAns3.getResult().getDescription().contains(Result.jsonParsingFailedErr),
                "ResultCode should be MalformedMessage and description should contain '" + Result.jsonParsingFailedErr + "'"
        );
    }
}
