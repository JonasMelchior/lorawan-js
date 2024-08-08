package com.github.jonasmelchior.js;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.lrwan.backendif.JoinAns;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.lrwan.JoinProcessor;
import com.github.jonasmelchior.js.service.user.UserService;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class JoinProcTests {

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
    private AppSKeyReqLogService appSKeyReqLogService;
    @Autowired
    private RunningJobService jobService;
    @Autowired
    private Utils utils;


    @org.junit.Test
    public void testCorrectJoinProcess1_0() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devIdService
        ), joinLogService, jobService, appSKeyReqLogService);

        utils.initTestDevice1_0();

        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070497," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"00F3BFBF\",\"PHYPayload\":\"000318AF9F5BCF4BD80103000000000000ADE74C3F6B8D\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService, jobService);
        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);

        Assert.isTrue(joinAns.getAppSKey().getaESKey(deviceKeyHandler.getKek()).equalsIgnoreCase("14BE5C9908B2ED7C4F3206517ED5B6CA"), "AppSKey derivation not correct");
        Assert.isTrue(joinAns.getNwkSKey().getaESKey(deviceKeyHandler.getKek()).equalsIgnoreCase("9D8108941A9C02B3E20D5B69104995A5"), "NwkSKey derivation not correct");

        Assert.isTrue(Hex.encodeHexString(deviceKeyHandler.getAppSKey("0000000000000301").getEncoded()).equalsIgnoreCase("14BE5C9908B2ED7C4F3206517ED5B6CA"), "AppSKey not stored correctly in key store");
        Assert.isTrue(Hex.encodeHexString(deviceKeyHandler.getNwkSKey("0000000000000301").getEncoded()).equalsIgnoreCase("9D8108941A9C02B3E20D5B69104995A5"), "NwkSKey not stored correctly in key store");

        // PHYPayload must either be 33 bytes (CFList present) or 17 bytes (CFList not present)
        // Note that MHDR is present in PHYPayload. That is, without MHDR Join-accept is a multiple of 16 no matter what
        Assert.isTrue(
                joinAns.getPHYPayload().length() == 66 ||
                        joinAns.getPHYPayload().length() == 34,
                "PHYPayload is not of correct size"
        );
    }

    @org.junit.Test
    public void testCorrectJoinProcess1_1() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devIdService
        ), joinLogService, jobService, appSKeyReqLogService);

        utils.initTestDevice1_1();

        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.1\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService, jobService);
        JoinAns joinAns = joinProcessor.processJoinReq(joinReqJson);

        Assert.isTrue(joinAns.getAppSKey().getaESKey(deviceKeyHandler.getKek()).length() == 32, "AppSKey is not 16 bytes");
        Assert.isTrue(joinAns.getfNwkSIntKey().getaESKey(deviceKeyHandler.getKek()).length() == 32, "FNwkSIntKey is not 16 bytes");
        Assert.isTrue(joinAns.getsNwkSIntKey().getaESKey(deviceKeyHandler.getKek()).length() == 32, "SNwkSIntKey is not 16 bytes");
        Assert.isTrue(joinAns.getNwkSEncKey().getaESKey(deviceKeyHandler.getKek()).length() == 32, "NwkSEncKey is not 16 bytes");


        Assert.isTrue(Hex.encodeHexString(deviceKeyHandler.getAppSKey("0000000000000301").getEncoded()).length() == 32, "AppSKey not of correct length from key store");
        Assert.isTrue(Hex.encodeHexString(deviceKeyHandler.getNwkSEncKey("0000000000000301").getEncoded()).length() == 32, "NwkSEncKey not of correct length from key store");
        Assert.isTrue(Hex.encodeHexString(deviceKeyHandler.getFNwkSIntKey("0000000000000301").getEncoded()).length() == 32, "FNwkSIntKey not of correct length from key store");
        Assert.isTrue(Hex.encodeHexString(deviceKeyHandler.getSNwkSIntKey("0000000000000301").getEncoded()).length() == 32, "SNwkSIntKey not of correct length from key store");

        // PHYPayload must either be 33 bytes (CFList present) or 17 bytes (CFList not present)
        // Note that MHDR is present in PHYPayload. That is, without MHDR Join-accept is a multiple of 16 no matter what
        Assert.isTrue(
                joinAns.getPHYPayload().length() == 66 ||
                        joinAns.getPHYPayload().length() == 34,
                "PHYPayload is not of correct size"
        );
    }

    @org.junit.Test
    public void testJoinNonceIncrement() {
        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"0000000000000003\",\"TransactionID\":3040474011," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"0108A99D\",\"PHYPayload\":\"0003000000000000000103000000000000211A6C88E523\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}\n";

        utils.initTestDevice1_0();

        Optional<Device> deviceOptional = deviceService.findByDevEUI("0000000000000301");

        int currentJoinNonce = Integer.parseInt(deviceOptional.get().getSessionStatus().getLastJoinNonce(), 16);
        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService, jobService);
        joinProcessor.processJoinReq(joinReqJson);

        Optional<Device> deviceOptional1 = deviceService.findByDevEUI("0000000000000301");
        int newJoinNonce = Integer.parseInt(deviceOptional1.get().getSessionStatus().getLastJoinNonce(), 16);

        Assert.isTrue(newJoinNonce == currentJoinNonce + 1, "JoinNonce has not been incremented after JoinAcc");
    }

    // Will be executed after each test function
    @After
    public void clearDB() {
        utils.clearDB();
    }

}
