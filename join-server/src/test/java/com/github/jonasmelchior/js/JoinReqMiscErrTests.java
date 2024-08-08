package com.github.jonasmelchior.js;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.lrwan.SessionKey;
import com.github.jonasmelchior.js.data.lrwan.backendif.JoinAns;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.lrwan.JoinProcessor;
import com.github.jonasmelchior.js.service.user.UserService;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
// To test all other errors than in validate() of JoinReq.
public class JoinReqMiscErrTests {
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setLenient()
            .create();


    @Autowired
    private DeviceService deviceService;
    @Autowired
    private KeyCredentialService keyCredentialService;
    @Autowired
    private DevKeyIdService devIdService;
    @Autowired
    private UserService userService;
    @Autowired
    private JoinLogService joinLogService;
    @Autowired
    private RunningJobService jobService;
    @Autowired
    private AppSKeyReqLogService appSKeyReqLogService;
    @Autowired
    private Utils utils;


    @org.junit.Test
    public void testReplayDetect() {
        utils.initTestDevice1_0();

        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService, jobService);
        joinProcessor.processJoinReq(joinReqJson);
        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);

        Assert.isTrue(joinAns.getResult().getResultCode().equals("FrameReplayed"), "Replay attempt not detected (duplicate DevNonces)");
    }

    @org.junit.Test
    public void testUnknownDevEUI() {
        utils.initTestDevice1_0();

        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000001\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        Optional<Device> optionalDevice = deviceService.findByDevEUI("0000000000000001");
        optionalDevice.ifPresent(device -> deviceService.delete(device));

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService, jobService);
        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);

        System.out.println(gson.toJson(joinAns.getResult()));

        Assert.isTrue(joinAns.getResult().getResultCode().equals("UnknownDevEUI"), "UnknowDevEUI Result not formed in JoinAns");
    }

    @org.junit.Test
    public void testJoinReqProcessingFailed() {
        utils.initTestDevice1_0();

        //CFList missing 1 byte
        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684be84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService, jobService);
        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);

        Assert.isTrue(joinAns.getResult().getResultCode().equals("JoinReqFailed"), "ResultCode should be JoinReqFailed");

        // Restore original test key
        SessionKey.testAppKey = "00000000000000000000000706050407";
    }

    @After
    public void clearDB() {
        utils.clearDB();
    }

}
