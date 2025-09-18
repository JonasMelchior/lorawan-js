package org.cibicom.iot.js;

import org.cibicom.iot.js.data.keys.KeySpec;
import org.cibicom.iot.js.data.keys.KeyType;
import org.cibicom.iot.js.data.lrwan.MACVersion;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.service.device.DevKeyIdService;
import org.cibicom.iot.js.service.device.keys.DeviceKeyHandler;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.device.keys.KeyHandler;
import org.cibicom.iot.js.service.log.JoinLogService;
import org.cibicom.iot.js.service.utils.RunningJobService;
import org.antlr.v4.runtime.misc.Pair;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.service.utils.init.JsInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@Profile("dev")
@EnableAsync
public class DevJoinServer implements CommandLineRunner {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private KeyCredentialService keyCredentialService;
    @Autowired
    private DevKeyIdService devIdService;
    @Autowired
    private UserService userService;
    Logger logger = LoggerFactory.getLogger(DevJoinServer.class);

    public static void main(String[] args) {
        SpringApplication.run(DevJoinServer.class, args);
    }

    // Initialization code for test devices, credentials and users
    @Override
    public void run(String... args) throws Exception {
        Security.addProvider(new BouncyCastleFipsProvider());

        JsInit jsInit = new JsInit(userService);

        jsInit.setMasterPassword();
        String adminEmail = jsInit.initInitialAdmin();
        jsInit.setKeystorePaths();
        jsInit.initKeyStores(new ArrayList<>(List.of(
                System.getProperty("keystore_filepath_credentials"),
                System.getProperty("keystore_filepath_rkeys"),
                System.getProperty("keystore_filepath_skeys"),
                System.getProperty("keystore_filepath_kek")
        )));

        // ### PLAYGROUND ###
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devIdService
                )
        );

        User user = null;
        Optional<User> optionalUserAdmin = userService.findByEmail(adminEmail);
        if (optionalUserAdmin.isEmpty()) {
            logger.error("Initial admin user has not been persisted. Contact system administrator");
            System.exit(1);
        }
        else {
            user = optionalUserAdmin.get();
        }

        User user1;
        Optional<User> userOptional1 = userService.findByEmail("jonas1@gmail.com");

        if (userOptional1.isEmpty()) {
            user1 = new User(
                    "jonas1@gmail.com",
                    "a_strong_pwd",
                    UserType.USER
            );
            userService.save(user1);
        }

        else {
            user1 = userOptional1.get();
        }


        // ESP32_SX1276
        if (deviceService.findByDevEUI("0000000000000301").isEmpty()) {
            Pair<Boolean, String> result = deviceKeyHandler.init(
                    new KeySpec("0000000000000301", "00000000000000000000000706050407", KeyType.AppKey1_0),
                    null,
                    "My_Test_Password1",
                    "Credential1",
                    user,
                    false,
                    MACVersion.LORAWAN_1_0,
                    true
            );
        }

        // Glamos field tester
        if (deviceService.findByDevEUI("1D4A7D0000927185").isEmpty()) {
            Pair<Boolean, String> result = deviceKeyHandler.init(
                    new KeySpec("1D4A7D0000927185", "2F6DE60C81548C210C50C4B408E68162", KeyType.AppKey1_0),
                    null,
                    "My_Test_Password2",
                    "Credential2",
                    user1,
                    false,
                    MACVersion.LORAWAN_1_0_2,
                    true
            );
        }

        // Test LoRaWAN 1.1
//        if (deviceService.findById("1D4A7ED000927185").isEmpty()) {
//            Pair<Boolean, String> result = deviceKeyHandler.init(
//                    new ArrayList<>(List.of(
//                            new KeySpec("1D4A7ED000927185", "3F6DE60C81548C210C50C4B408E68161", KeyType.AppKey1_1),
//                            new KeySpec("1D4A7ED000927185", "4F6DE60C81548C210C50C4B408E68162", KeyType.NwkKey1_1)
//                    )),
//                    "My_Test_Password3",
//                    "Credential4",
//                    user,
//                    false,
//                    MACVersion.LORAWAN_1_1
//            );
//        }
    }


}
