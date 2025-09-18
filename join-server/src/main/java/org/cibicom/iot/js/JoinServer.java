package org.cibicom.iot.js;

import org.cibicom.iot.js.data.keys.KeySpec;
import org.cibicom.iot.js.data.keys.KeyType;
import org.cibicom.iot.js.data.lrwan.MACVersion;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.service.device.DevKeyIdService;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.service.device.keys.DeviceKeyHandler;
import org.cibicom.iot.js.service.device.keys.KeyHandler;
import org.antlr.v4.runtime.misc.Pair;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.cibicom.iot.js.service.utils.init.JsInit;
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
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(JoinServer.class, args);
    }

    Logger logger = LoggerFactory.getLogger(JoinServer.class);

    @Override
    public void run(String... args) throws Exception {
        Security.addProvider(new BouncyCastleFipsProvider());

        JsInit jsInit = new JsInit(userService);

        jsInit.setMasterPassword();
        jsInit.initInitialAdmin();
        jsInit.setKeystorePaths();
        jsInit.initKeyStores(new ArrayList<>(List.of(
                System.getProperty("keystore_filepath_credentials"),
                System.getProperty("keystore_filepath_rkeys"),
                System.getProperty("keystore_filepath_skeys"),
                System.getProperty("keystore_filepath_kek")
        )));
    }
}
