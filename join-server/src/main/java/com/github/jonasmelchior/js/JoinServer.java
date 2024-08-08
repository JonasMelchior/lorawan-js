package com.github.jonasmelchior.js;

import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.KeyType;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.data.user.UserType;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.user.UserService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import org.antlr.v4.runtime.misc.Pair;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@Profile("js")
@EnableAsync
public class JoinServer implements CommandLineRunner {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private KeyCredentialService keyCredentialService;
    @Autowired
    private DevKeyIdService devIdService;
    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(JoinServer.class, args);
    }

    Logger logger = LoggerFactory.getLogger(JoinServer.class);

    @Override
    public void run(String... args) throws Exception {
        Security.addProvider(new BouncyCastleFipsProvider());

        Map<String, Object> data = null;
        boolean configFileSpecified = true;

        try {
            if (System.getProperty("js_config") != null) {
                Yaml yaml = new Yaml();
                FileInputStream inputStream = new FileInputStream(System.getProperty("js_config"));
                data = yaml.load(inputStream);
            }
            else {
                logger.info("No config file specified");
                configFileSpecified = false;
            }
        } catch (FileNotFoundException e) {
            logger.info("Config file couldn't be found");
            configFileSpecified = false;
        }

        if (configFileSpecified) {
            Map<String, String> pathsMap = (Map<String, String>) data.get("storage");
            System.setProperty("keystore_filepath_credentials", pathsMap.get("credentials") + "/credential_keys.bcfks");
            System.setProperty("keystore_filepath_rkeys", pathsMap.get("rkeys") + "/rkeys.bcfks");
            System.setProperty("keystore_filepath_skeys", pathsMap.get("skeys") + "/skeys.bcfks");
            System.setProperty("keystore_filepath_kek", pathsMap.get("keks") + "/kek.bcfks");
        }
        else {
            String userHome = System.getProperty("user.home");
            String defaultPath = userHome + File.separator + ".lrwan_js";

            File directory = new File(defaultPath);
            if (!directory.exists()) {
                logger.info("Creating ~/.lrwan_js default directory");
                directory.mkdir();
            }
            System.setProperty("keystore_filepath_credentials", defaultPath + "/credential_keys.bcfks");
            System.setProperty("keystore_filepath_rkeys", defaultPath + "/rkeys.bcfks");
            System.setProperty("keystore_filepath_skeys", defaultPath + "/skeys.bcfks");
            System.setProperty("keystore_filepath_kek", defaultPath + "/kek.bcfks");
        }

        System.setProperty("master_pwd", "mighty_prod_pass");

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

        //TODO: change this - insecure for prod
        deviceKeyHandler.setKek("c4af61abf4cc2d9cab83fc92a9d6dc4056d8b266b68770a69d40cb7c4918f991");

        Optional<User> userOptional = userService.findByEmail("admin@gmail.com");
        User user;
        if (userOptional.isEmpty()) {
            user = new User(
                    "admin@gmail.com",
                    "admin",
                    UserType.USER
            );
            userService.save(user);
        }
        else {
            user = userOptional.get();
        }

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
