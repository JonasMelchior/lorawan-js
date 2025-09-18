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
// To test all errors that can occur in validate() of the JoinReq class
public class JoinReqValidatorTests {

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
    public void testNullFields() {
        // SenderID is missing
        String joinReqJson1_0 = "{\"ProtocolVersion\":\"1.0\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        // SenderNSID is missing
        String joinReqJson1_1 = "{\"ProtocolVersion\":\"1.1\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService);
        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson1_0);
        JoinAns joinAns1 = joinProcessor.processJoinReq(joinReqJson1_1);

        Assert.isTrue(
                joinAns.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAns.getResult().getDescription().equals(Result.nullErrManFields1_0),
                "ResultCode is not MalformedMessage or description is not " + Result.nullErrManFields1_0
        );
        Assert.isTrue(
                joinAns1.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAns1.getResult().getDescription().equals(Result.nullErrManFields1_1),
                "ResultCode is not MalformedMessage or description is not " + Result.nullErrManFields1_1
        );
    }

    @Test
    public void testInvalidProtocolVersion() {
        // SenderNSID is missing
        String joinReqJson = "{\"ProtocolVersion\":\"1.2\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";


        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService);
        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);

        Assert.isTrue(
                joinAns.getResult().getResultCode().equals("InvalidProtocolVersion") &&
                        joinAns.getResult().getDescription().equals(Result.wrongProtVersionErr),
                "ResultCode is not InvalidProtocolVersion or description is not " + Result.wrongProtVersionErr
        );
    }

    @Test
    public void testLengthErr() {
        String joinReqSenderIDS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"00000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqReceiverIDS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqDevEUIS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqDevAddrS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"010899D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqDLSettingsS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"0\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqPHYPayloadS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"000300000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService);

        JoinAns joinAnsSenderID = joinProcessor.processJoinReq(joinReqSenderIDS);
        JoinAns joinAnsReceiverID = joinProcessor.processJoinReq(joinReqReceiverIDS);
        JoinAns joinAnsDevEUI = joinProcessor.processJoinReq(joinReqDevEUIS);
        JoinAns joinAnsDevAddr = joinProcessor.processJoinReq(joinReqDevAddrS);
        JoinAns joinAnsDLSettings = joinProcessor.processJoinReq(joinReqDLSettingsS);
        JoinAns joinAnsPHYPayload = joinProcessor.processJoinReq(joinReqPHYPayloadS);

        Assert.isTrue(
                joinAnsSenderID.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsSenderID.getResult().getDescription().equals(Result.lengthErrSenderId),
                "ResultCode is not MalformedMessage or description is not " + Result.lengthErrSenderId
        );
        Assert.isTrue(
                joinAnsReceiverID.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsReceiverID.getResult().getDescription().equals(Result.lengthErrReceiverId),
                "ResultCode is not MalformedMessage or description is not " + Result.lengthErrReceiverId
        );
        Assert.isTrue(
                joinAnsDevEUI.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsDevEUI.getResult().getDescription().equals(Result.lengthErrDevEUI),
                "ResultCode is not MalformedMessage or description is not " + Result.lengthErrDevEUI
        );
        Assert.isTrue(
                joinAnsDevAddr.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsDevAddr.getResult().getDescription().equals(Result.lengthErrDevAddr),
                "ResultCode is not MalformedMessage or description is not " + Result.lengthErrDevAddr
        );
        Assert.isTrue(
                joinAnsDLSettings.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsDLSettings.getResult().getDescription().equals(Result.lengthErrDLSettings),
                "ResultCode is not MalformedMessage or description is not " + Result.lengthErrDLSettings
        );
        Assert.isTrue(
                joinAnsPHYPayload.getResult().getResultCode().equals("FrameSizeError") &&
                        joinAnsPHYPayload.getResult().getDescription().equals(Result.lengthErrPHYPayload),
                "ResultCode is not FrameSizeError or description is not " + Result.lengthErrPHYPayload
        );
    }

    @Test
    public void testContentErr() {
        String joinReqSenderIDS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"00A00Q\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqReceiverIDS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000A00B000[0003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqDevEUIS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"00000#000Q000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqDevAddrS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"z1'8A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqDLSettingsS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"X0\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqPHYPayloadS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"Q003000000000I00000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";
        String joinReqMessageTypeS = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinSomething\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService);

        JoinAns joinAnsSenderID = joinProcessor.processJoinReq(joinReqSenderIDS);
        JoinAns joinAnsReceiverID = joinProcessor.processJoinReq(joinReqReceiverIDS);
        JoinAns joinAnsDevEUI = joinProcessor.processJoinReq(joinReqDevEUIS);
        JoinAns joinAnsDevAddr = joinProcessor.processJoinReq(joinReqDevAddrS);
        JoinAns joinAnsDLSettings = joinProcessor.processJoinReq(joinReqDLSettingsS);
        JoinAns joinAnsPHYPayload = joinProcessor.processJoinReq(joinReqPHYPayloadS);
        JoinAns joinAnsMessageType = joinProcessor.processJoinReq(joinReqMessageTypeS);

        Assert.isTrue(
                joinAnsSenderID.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsSenderID.getResult().getDescription().equals(Result.contentErrSenderId),
                "ResultCode is not MalformedMessage or description is not " + Result.contentErrSenderId
        );
        Assert.isTrue(
                joinAnsReceiverID.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsReceiverID.getResult().getDescription().equals(Result.contentErrReceiverId),
                "ResultCode is not MalformedMessage or description is not " + Result.contentErrReceiverId
        );
        Assert.isTrue(
                joinAnsDevEUI.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsDevEUI.getResult().getDescription().equals(Result.contentErrDevEUI),
                "ResultCode is not MalformedMessage or description is not " + Result.contentErrDevEUI
        );
        Assert.isTrue(
                joinAnsDevAddr.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsDevAddr.getResult().getDescription().equals(Result.contentErrDevAddr),
                "ResultCode is not MalformedMessage or description is not " + Result.contentErrDevAddr
        );
        Assert.isTrue(
                joinAnsDLSettings.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsDLSettings.getResult().getDescription().equals(Result.contentErrDLSettings),
                "ResultCode is not MalformedMessage or description is not " + Result.contentErrDLSettings
        );
        Assert.isTrue(
                joinAnsPHYPayload.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsPHYPayload.getResult().getDescription().equals(Result.contentErrPHYPayload),
                "ResultCode is not FrameSizeError or description is not " + Result.contentErrPHYPayload
        );
        Assert.isTrue(
                joinAnsMessageType.getResult().getResultCode().equals("MalformedMessage") &&
                        joinAnsMessageType.getResult().getDescription().equals(Result.contentErrMessageType),
                "ResultCode is not FrameSizeError or description is not " + Result.contentErrMessageType
        );
    }
}
