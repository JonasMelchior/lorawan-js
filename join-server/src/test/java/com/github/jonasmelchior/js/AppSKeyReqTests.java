package com.github.jonasmelchior.js;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.KeyType;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.lrwan.backendif.AppSKeyAns;
import com.github.jonasmelchior.js.data.lrwan.backendif.Result;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.data.user.UserType;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.lrwan.AppSKeyReqHandler;
import com.github.jonasmelchior.js.service.lrwan.JoinProcessor;
import com.github.jonasmelchior.js.service.user.UserService;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AppSKeyReqTests {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private KeyCredentialService keyCredentialService;
    @Autowired
    private DevKeyIdService devKeyIdService;
    @Autowired
    private JoinLogService joinLogService;
    @Autowired
    private RunningJobService jobService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppSKeyReqLogService appSKeyReqLogService;
    @Autowired
    Utils utils;

    @Test
    public void testAppSKeyReq() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devKeyIdService
        ), joinLogService, jobService, appSKeyReqLogService);

        utils.initTestDevice1_0();

        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070497," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"00F3BFBF\",\"PHYPayload\":\"000318AF9F5BCF4BD80103000000000000ADE74C3F6B8D\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}";

        String appSKeyReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070498," +
                "\"MessageType\":\"AppSKeyReq\", \"DevEUI\":\"0000000000000301\",\"SessionKeyID\":\"1\"}";

        // We first need to process a JoinReq in order to initialize and AppSKey
        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devKeyIdService, joinLogService, jobService);
        joinProcessor.processJoinReq(joinReqJson);

        AppSKeyReqHandler appSKeyReqHandler = new AppSKeyReqHandler(deviceService, keyCredentialService, devKeyIdService, appSKeyReqLogService);
        AppSKeyAns appSKeyAns = appSKeyReqHandler.processAppSKeyReq(appSKeyReqJson);

        Assert.assertEquals("Success", appSKeyAns.getResult().getResultCode());
        //Assert.assertEquals(KeyEnvelope.unwrap(appSKeyAns.getAppSKey().getaESKey(), deviceKeyHandler.getKek()));
    }

    @Test
    public void testUnknownDevEUI() {
        String appSKeyReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070498," +
                "\"MessageType\":\"AppSKeyReq\", \"DevEUI\":\"00AB000000000401\",\"SessionKeyID\":\"1\"}";

        AppSKeyReqHandler appSKeyReqHandler = new AppSKeyReqHandler(deviceService, keyCredentialService, devKeyIdService, appSKeyReqLogService);
        AppSKeyAns appSKeyAns = appSKeyReqHandler.processAppSKeyReq(appSKeyReqJson);

        Assert.assertEquals("UnknownDevEUI", appSKeyAns.getResult().getResultCode());
    }

    @Test
    public void testNotActivated() {
        utils.initTestDevice1_0();

        String appSKeyReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070498," +
                "\"MessageType\":\"AppSKeyReq\", \"DevEUI\":\"0000000000000301\",\"SessionKeyID\":\"1\"}";

        AppSKeyReqHandler appSKeyReqHandler = new AppSKeyReqHandler(deviceService, keyCredentialService, devKeyIdService, appSKeyReqLogService);
        AppSKeyAns appSKeyAns = appSKeyReqHandler.processAppSKeyReq(appSKeyReqJson);

        Assert.assertTrue(appSKeyAns.getResult().getResultCode().equals("Other") &&
                appSKeyAns.getResult().getDescription().equals("Device " + appSKeyAns.getDevEUI() + " has not been activated yet"));
    }

    @Test
    public void testIncorrectSessionKeyID() {
        utils.initTestDevice1_0();

        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070497," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"00F3BFBF\",\"PHYPayload\":\"000318AF9F5BCF4BD80103000000000000ADE74C3F6B8D\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}";

        String appSKeyReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070498," +
                "\"MessageType\":\"AppSKeyReq\", \"DevEUI\":\"0000000000000301\",\"SessionKeyID\":\"2\"}";

        // We first need to process a JoinReq in order to initialize and AppSKey
        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devKeyIdService, joinLogService, jobService);
        joinProcessor.processJoinReq(joinReqJson);

        AppSKeyReqHandler appSKeyReqHandler = new AppSKeyReqHandler(deviceService, keyCredentialService, devKeyIdService, appSKeyReqLogService);
        AppSKeyAns appSKeyAns = appSKeyReqHandler.processAppSKeyReq(appSKeyReqJson);

        Assert.assertTrue(appSKeyAns.getResult().getResultCode().equals("Other") &&
                appSKeyAns.getResult().getDescription().equals("Specified sessionKeyID is not correct for DevEUI " + appSKeyAns.getDevEUI()));
    }

    @Test
    public void testIncorrectJSON() {
        String appSKeyReqJson = "{\"ProtocolVersion\"\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070498," +
                "\"MessageType\":\"AppSKeyReq\", \"DevEUI\":\"0000000000000301\",\"SessionKeyID\":\"1\"}";

        AppSKeyReqHandler appSKeyReqHandler = new AppSKeyReqHandler(deviceService, keyCredentialService, devKeyIdService, appSKeyReqLogService);
        AppSKeyAns appSKeyAns = appSKeyReqHandler.processAppSKeyReq(appSKeyReqJson);

        Assert.assertTrue(appSKeyAns.getResult().getResultCode().equals("MalformedMessage") &&
                appSKeyAns.getResult().getDescription().contains(Result.jsonParsingFailedErr));
    }


    private boolean initTestDevice1_0() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devKeyIdService
        ), joinLogService, jobService, appSKeyReqLogService);

        Optional<User> userOptional = userService.findByEmail("jonas@gmail.com");
        User user;

        if (userOptional.isEmpty()) {
            user = new User(
                    "jonas@gmail.com",
                    "a_strong_pwd",
                    UserType.USER
            );
            userService.save(user);
        }
        else {
            user = userOptional.get();
        }

        Optional<Device> optionalDevice = deviceService.findByDevEUI("0000000000000301");
        if (optionalDevice.isEmpty()) {
            deviceKeyHandler.init(
                    new KeySpec("0000000000000301", "00000000000000000000000706050407", KeyType.AppKey1_0),
                    "My_Test_Password1",
                    "Credential1",
                    user,
                    false,
                    MACVersion.LORAWAN_1_0

            );
            return true;
        }

        return false;
    }
    // Will be executed after each test function
    @After
    public void clearDB() {
        utils.clearDB();
    }
}
