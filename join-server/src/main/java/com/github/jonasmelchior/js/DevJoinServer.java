package com.github.jonasmelchior.js;

import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.KeyType;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.data.user.UserType;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
import org.antlr.v4.runtime.misc.Pair;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import com.github.jonasmelchior.js.service.user.UserService;
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
import java.util.Random;

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
    @Autowired
    private JoinLogService joinLogService;
    @Autowired
    private RunningJobService jobService;
    Logger logger = LoggerFactory.getLogger(DevJoinServer.class);

    public static void main(String[] args) {
        SpringApplication.run(DevJoinServer.class, args);
    }

    // Initialization code for test devices, credentials and users
    @Override
    public void run(String... args) throws Exception {
        Security.addProvider(new BouncyCastleFipsProvider());

        System.setProperty("master_pwd", "mighty_prod_pass");

        String userHome = System.getProperty("user.home");
        String defaultPath = userHome + File.separator + ".lrwan_js";

        System.setProperty("keystore_filepath_credentials", defaultPath + File.separator + "credential_keys.bcfks");
        System.setProperty("keystore_filepath_rkeys", defaultPath + File.separator + "rkeys.bcfks");
        System.setProperty("keystore_filepath_skeys", defaultPath + File.separator + "skeys.bcfks");
        System.setProperty("keystore_filepath_kek", defaultPath + File.separator + "kek.bcfks");
        File directory = new File(defaultPath);
        if (!directory.exists()) {
            logger.info("Creating ~/.lrwan_js default directory");
            directory.mkdir();
        }

        initKeyStores(new ArrayList<>(List.of(
                System.getProperty("keystore_filepath_credentials"),
                System.getProperty("keystore_filepath_rkeys"),
                System.getProperty("keystore_filepath_skeys"),
                System.getProperty("keystore_filepath_kek")
        )));

        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devIdService
                )
        );

        deviceKeyHandler.setKek("c4af61abf4cc2d9cab83fc92a9d6dc4056d8b266b68770a69d40cb7c4918f991");

        Optional<User> userOptional = userService.findByEmail("jonas@gmail.com");
        Optional<User> userOptional1 = userService.findByEmail("jonas1@gmail.com");

        User user;
        User user1;

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

        String filePath = System.getProperty("keystore_filepath_credentials");
        String filePath1 = System.getProperty("keystore_filepath_rkeys");
        String filePath2 = System.getProperty("keystore_filepath_skeys");
        String filePath3 = System.getProperty("keystore_filepath_kek");

        // Create a File object with the specified path
        File file = new File(filePath);
        File file1 = new File(filePath1);
        File file2 = new File(filePath2);
        File file3 = new File(filePath3);

        if (!file.exists()) {
            KeyStore keyStore = KeyStore.getInstance("BCFKS", "BCFIPS");
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file), System.getProperty("master_pwd").toCharArray());
        }
        if (!file1.exists()) {
            KeyStore keyStore = KeyStore.getInstance("BCFKS", "BCFIPS");
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file1), System.getProperty("master_pwd").toCharArray());
        }
        if (!file2.exists()) {
            KeyStore keyStore = KeyStore.getInstance("BCFKS", "BCFIPS");
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file2), System.getProperty("master_pwd").toCharArray());
        }
        if (!file3.exists()) {
            KeyStore keyStore = KeyStore.getInstance("BCFKS", "BCFIPS");
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file3), System.getProperty("master_pwd").toCharArray());
        }

        // ESP32_SX1276
        if (deviceService.findByDevEUI("0000000000000301").isEmpty()) {
            Pair<Boolean, String> result = deviceKeyHandler.init(
                    new KeySpec("0000000000000301", "00000000000000000000000706050407", KeyType.AppKey1_0),
                    "My_Test_Password1",
                    "Credential1",
                    user,
                    false,
                    MACVersion.LORAWAN_1_0
            );
        }

        // Glamos field tester
        if (deviceService.findByDevEUI("1D4A7D0000927185").isEmpty()) {
            Pair<Boolean, String> result = deviceKeyHandler.init(
                    new KeySpec("1D4A7D0000927185", "2F6DE60C81548C210C50C4B408E68162", KeyType.AppKey1_0),
                    "My_Test_Password2",
                    "Credential2",
                    user1,
                    false,
                    MACVersion.LORAWAN_1_0_2
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

    private void initKeyStores(List<String> files) {
        for (String filePath : files) {
            logger.info("Initializing " + filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    KeyStore keyStore = KeyStore.getInstance("BCFKS", "BCFIPS");
                    keyStore.load(null, null);
                    keyStore.store(new FileOutputStream(file), System.getProperty("master_pwd").toCharArray());
                } catch (KeyStoreException | NoSuchProviderException | CertificateException | IOException |
                         NoSuchAlgorithmException e) {
                    logger.error("Failed to initialize key stores: " + e.getMessage());
                    System.exit(1);
                }
            }
            else {
                logger.info("Key Store exists");
            }
        }
    }

}
