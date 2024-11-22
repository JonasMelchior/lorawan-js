package com.github.jonasmelchior.js;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.DeviceGroup;
import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.KeyType;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.data.user.UserType;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.user.UserService;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceKeyHandlerTests {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private KeyCredentialService keyCredentialService;

    @Autowired
    private UserService userService;

    @Autowired
    private DevKeyIdService devIdService;
    @Autowired
    private JoinLogService joinLogService;
    @Autowired
    private RunningJobService jobService;
    @Autowired
    private AppSKeyReqLogService appSKeyReqLogService;


    String testDevEUI = "98732AB235ED45A8";
    String testAppKey = "09932AB235ED45A09932AB235ED45A98";
    String testNwkKey = "09932AB835ED45A09932AB235ED45A98";
    String testDevEUI1 = "09932AB235E325A8";
    String testAppKey1 = "09932AB235ED45A09932AB2357645A98";
    String testNwkKey1 = "09932AB2356745A09932AB235ED45A98";
    String testDevEUI2 = "09932A2E35ED4098";
    String testAppKey2 = "01232AB235ED45A09932AB2ABED45A98";
    String testNwkKey2 = "09932AB235ED45A09932AB2359045A98";

    String testCredential = "Registration Test";
    String testCredentialUpdate = "Updated Credential";

    DeviceGroup deviceGroup = new DeviceGroup(
            "Test Group"
    );

    @Test
    public void testDeviceRegistration1_0() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(keyCredentialService, devIdService),
                joinLogService,
                jobService,
                appSKeyReqLogService
        );

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

        deviceKeyHandler.init(
                new KeySpec(
                        testDevEUI,
                        testAppKey,
                        KeyType.AppKey1_0
                ),
                null,
                "12345",
                testCredential,
                user,
                false,
                MACVersion.LORAWAN_1_0
        );

        Key key = deviceKeyHandler.getAppKey1_0(testDevEUI);

        Assert.assertEquals(Hex.encodeHexString(key.getEncoded()).toUpperCase(), testAppKey);
    }

    @Test
    public void testDeviceRegistration1_1() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(keyCredentialService, devIdService),
                joinLogService,
                jobService,
                appSKeyReqLogService
        );

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

        deviceKeyHandler.init(
                new ArrayList<>(List.of(
                        new KeySpec(testDevEUI, testAppKey, KeyType.AppKey1_1),
                        new KeySpec(testDevEUI, testNwkKey, KeyType.NwkKey1_1)
                )),
                null,
                "12345",
                testCredential,
                user,
                false,
                MACVersion.LORAWAN_1_1
        );

        Key appKey = deviceKeyHandler.getAppKey1_1(testDevEUI);
        Key nwkKey = deviceKeyHandler.getNwkKey1_1(testDevEUI);

        Assert.assertEquals(Hex.encodeHexString(appKey.getEncoded()).toUpperCase(), testAppKey);
        Assert.assertEquals(Hex.encodeHexString(nwkKey.getEncoded()).toUpperCase(), testNwkKey);
    }

    @Test
    public void testMultipleDeviceRegistration1_0() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(keyCredentialService, devIdService),
                joinLogService,
                jobService,
                appSKeyReqLogService
        );

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

        deviceKeyHandler.init(
                new ArrayList<>(List.of(
                        new KeySpec(testDevEUI, testAppKey, KeyType.AppKey1_0),
                        new KeySpec(testDevEUI1, testAppKey1, KeyType.AppKey1_0),
                        new KeySpec(testDevEUI2, testAppKey2, KeyType.AppKey1_0)
                )),
                null,
                "12345",
                testCredential,
                user,
                false,
                MACVersion.LORAWAN_1_0
        );

        Key key = deviceKeyHandler.getAppKey1_0(testDevEUI);
        Key key1 = deviceKeyHandler.getAppKey1_0(testDevEUI1);
        Key key2 = deviceKeyHandler.getAppKey1_0(testDevEUI2);


        Assert.assertEquals(Hex.encodeHexString(key.getEncoded()).toUpperCase(), testAppKey);
        Assert.assertEquals(Hex.encodeHexString(key1.getEncoded()).toUpperCase(), testAppKey1);
        Assert.assertEquals(Hex.encodeHexString(key2.getEncoded()).toUpperCase(), testAppKey2);

    }

    @Test
    public void testMultipleDeviceRegistration1_1() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(keyCredentialService, devIdService),
                joinLogService,
                jobService,
                appSKeyReqLogService
        );

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

        Pair<Boolean, String> result = deviceKeyHandler.init(
                new ArrayList<>(List.of(
                        new KeySpec(testDevEUI, testAppKey, KeyType.AppKey1_1),
                        new KeySpec(testDevEUI, testNwkKey, KeyType.NwkKey1_1),
                        new KeySpec(testDevEUI1, testAppKey1, KeyType.AppKey1_1),
                        new KeySpec(testDevEUI1, testNwkKey1, KeyType.NwkKey1_1),
                        new KeySpec(testDevEUI2, testAppKey2, KeyType.AppKey1_1),
                        new KeySpec(testDevEUI2, testNwkKey2, KeyType.NwkKey1_1)
                )),
                null,
                "12345",
                testCredential,
                user,
                false,
                MACVersion.LORAWAN_1_1
        );

        Key appKey = deviceKeyHandler.getAppKey1_1(testDevEUI);
        Key appKey1 = deviceKeyHandler.getAppKey1_1(testDevEUI1);
        Key appKey2 = deviceKeyHandler.getAppKey1_1(testDevEUI2);
        Key nwkKey = deviceKeyHandler.getNwkKey1_1(testDevEUI);
        Key nwkKey1 = deviceKeyHandler.getNwkKey1_1(testDevEUI1);
        Key nwkKey2 = deviceKeyHandler.getNwkKey1_1(testDevEUI2);


        Assert.assertEquals(Hex.encodeHexString(appKey.getEncoded()).toUpperCase(), testAppKey);
        Assert.assertEquals(Hex.encodeHexString(appKey1.getEncoded()).toUpperCase(), testAppKey1);
        Assert.assertEquals(Hex.encodeHexString(appKey2.getEncoded()).toUpperCase(), testAppKey2);
        Assert.assertEquals(Hex.encodeHexString(nwkKey.getEncoded()).toUpperCase(), testNwkKey);
        Assert.assertEquals(Hex.encodeHexString(nwkKey1.getEncoded()).toUpperCase(), testNwkKey1);
        Assert.assertEquals(Hex.encodeHexString(nwkKey2.getEncoded()).toUpperCase(), testNwkKey2);

    }

    @Test
    public void testCredentialUpdate() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(keyCredentialService, devIdService),
                joinLogService,
                jobService,
                appSKeyReqLogService
        );

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

        deviceKeyHandler.init(
                new KeySpec(
                        testDevEUI1,
                        testAppKey1,
                        KeyType.AppKey1_0
                ),
                null,
                "My_Test_Password2",
                testCredentialUpdate,
                user,
                false,
                MACVersion.LORAWAN_1_0
        );

        Optional<KeyCredential> keyCredential = keyCredentialService.findById("Updated Credential");
        deviceKeyHandler.init(
                new KeySpec(
                        testDevEUI2,
                        testAppKey2,
                        KeyType.AppKey1_0
                ),
                null,
                keyCredential.get(),
                user,
                false,
                MACVersion.LORAWAN_1_0
        );

        Assert.assertEquals("Updated Credential", devIdService.findCredentialByDevEUI(testDevEUI1).get().getIdentifier());
        Assert.assertEquals("Updated Credential", devIdService.findCredentialByDevEUI(testDevEUI2).get().getIdentifier());

    }

    @Test
    public void testDelete() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(keyCredentialService, devIdService),
                joinLogService,
                jobService,
                appSKeyReqLogService
        );

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

        deviceKeyHandler.init(
                new KeySpec(
                        testDevEUI,
                        testAppKey,
                        KeyType.AppKey1_0
                ),
                null,
                "12345",
                testCredential,
                user,
                false,
                MACVersion.LORAWAN_1_0
        );

        Assert.assertNotNull(deviceKeyHandler.getAppKey1_0(testDevEUI));

        Pair<Boolean, String> result = deviceKeyHandler.delete(deviceService.findByDevEUI(testDevEUI).get());

        Assert.assertTrue(result.a);
        Assert.assertNull(deviceKeyHandler.getAppKey1_0(testDevEUI));
        Assert.assertTrue(devIdService.findByDevEUI(testDevEUI).isEmpty());
        Assert.assertTrue(this.deviceService.findByDevEUI(testDevEUI).isEmpty());
    }

//    @Test
//    public void testKek() throws JoinReqFailedExc {
//        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
//                deviceService,
//                new KeyHandler(keyCredentialService, devIdService),
//                joinLogService,
//                jobService,
//                appSKeyReqLogService
//        );
//
//        Assert.assertNotNull(deviceKeyHandler.getKek());
//
//        KeyEnvelope keyEnvelope = new KeyEnvelope("6FA41E2B7C25D8F3A914B6C0FED7A8C9", deviceKeyHandler.getKek());
//        Assert.assertNotEquals("6FA41E2B7C25D8F3A914B6C0FED7A8C9", keyEnvelope.getaESKey().toUpperCase());
//        Assert.assertEquals("6FA41E2B7C25D8F3A914B6C0FED7A8C9", keyEnvelope.getaESKey(deviceKeyHandler.getKek()).toUpperCase());
//    }

    private String generateRandomHexString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomNumber = random.nextInt(16); // Generate random number between 0 and 15
            char hexChar = Character.forDigit(randomNumber, 16); // Convert to hexadecimal character
            sb.append(hexChar);
        }

        return sb.toString();
    }

    @After
    public void clearDB() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(keyCredentialService, devIdService),
                joinLogService,
                jobService,
                appSKeyReqLogService
        );

        deviceService.findByDevEUI("0000000000000301").ifPresent(deviceKeyHandler::delete);
        keyCredentialService.findById("Credential1").ifPresent(keyCredentialService::delete);

        Optional<Device> device = this.deviceService.findByDevEUI(testDevEUI);
        Optional<Device> device1 = this.deviceService.findByDevEUI(testDevEUI1);
        Optional<Device> device2 = this.deviceService.findByDevEUI(testDevEUI2);

        device.ifPresent(deviceKeyHandler::delete);
        device1.ifPresent(deviceKeyHandler::delete);
        device2.ifPresent(deviceKeyHandler::delete);

        Optional<KeyCredential> keyCredential = keyCredentialService.findById(testCredential);
        Optional<KeyCredential> keyCredential1 = keyCredentialService.findById(testCredentialUpdate);

        keyCredential.ifPresent(keyCredentialService::delete);
        keyCredential1.ifPresent(keyCredentialService::delete);

        Optional<User> user = userService.findByEmail("jonas@gmail.com");
        user.ifPresent(userService::delete);
    }

    private void initDeviceGroup() {

    }
}
