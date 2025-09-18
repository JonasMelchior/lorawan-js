package org.cibicom.iot.js.service.utils.init;

import org.cibicom.iot.js.JoinServer;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsInit {

    private final Logger logger = LoggerFactory.getLogger(JsInit.class);

    private Map<String, Object> configData = new HashMap<>();
    private boolean configFileSpecified = true;

    private UserService userService;

    public JsInit(UserService userService) {
        this.userService = userService;
        try {
            if (System.getProperty("js_config") != null) {
                Yaml yaml = new Yaml();
                FileInputStream inputStream = new FileInputStream(System.getProperty("js_config"));
                configData = yaml.load(inputStream);
            }
            else {
                logger.info("No config file specified");
                configFileSpecified = false;
            }
        } catch (FileNotFoundException e) {
            logger.error("Provided JS config file couldn't be found");
            System.exit(1);
        } catch (ScannerException e) {
            logger.error("JS Config file couldn't be parsed. It must follow yaml format.");
            System.exit(1);
        }
    }


    public void setMasterPassword() {
        if (configFileSpecified && configData.get("master_pwd") != null) {
            String masterPwd = (String) configData.get("master_pwd");
            logger.info("Setting configured password");
            if (masterPwd.length() < 16) {
                logger.error("Password must have at least 16 characters");
                System.exit(1);
            }
            else {
                System.setProperty("master_pwd", masterPwd);
            }

        }
        else {
            logger.info("Setting default password");
            System.setProperty("master_pwd", "mighty_prod_pass");
        }
    }

    public void setKeystorePaths() {
        if (configFileSpecified && configData.get("storage") != null) {
            logger.info("Setting configured keystore paths");
            Map<String, String> keystorePaths = null;
            try {
                keystorePaths = (Map<String, String>) configData.get("storage");
            } catch (ClassCastException e) {
                logger.error("Storage entry expects 4 child entries [credentials, rkeys, skeys, keks]");
                System.exit(1);
            }

            if (!keystorePaths.containsKey("credentials") || !keystorePaths.containsKey("rkeys") ||
                    !keystorePaths.containsKey("skeys") || !keystorePaths.containsKey("keks")) {
                logger.error("All keystore paths have not been specified [credentials, rkeys, skeys, keks]");
                System.exit(1);
            }
            System.setProperty("keystore_filepath_credentials", keystorePaths.get("credentials") + "/credential_keys.bcfks");
            System.setProperty("keystore_filepath_rkeys", keystorePaths.get("rkeys") + "/rkeys.bcfks");
            System.setProperty("keystore_filepath_skeys", keystorePaths.get("skeys") + "/skeys.bcfks");
            System.setProperty("keystore_filepath_kek", keystorePaths.get("keks") + "/kek.bcfks");
        }
        else {
            logger.info("Setting default keystore paths");
            String userHome = System.getProperty("user.home");
            String defaultPath = userHome + File.separator + ".lrwan_js";

            File directory = new File(defaultPath);
            if (!directory.exists()) {
                logger.info("Creating ~/.lrwan_js default directory");
                boolean directoryCreated = directory.mkdir();
                if (!directoryCreated) {
                    logger.error("Failed to create default directory at " + defaultPath);
                    System.exit(1);
                }
            }

            System.setProperty("keystore_filepath_credentials", defaultPath + "/credential_keys.bcfks");
            System.setProperty("keystore_filepath_rkeys", defaultPath + "/rkeys.bcfks");
            System.setProperty("keystore_filepath_skeys", defaultPath + "/skeys.bcfks");
            System.setProperty("keystore_filepath_kek", defaultPath + "/kek.bcfks");
        }
    }

    public String initInitialAdmin() {
        if (configFileSpecified && configData.get("admin") != null) {
            logger.info("Setting configured initial admin user");
            // First entry: Username (email)
            // Second entry: Password (hashed with bcrypt)
            Map<String, String> adminUser = null;
            try {
                adminUser = (Map<String, String>) configData.get("admin");
            } catch (ClassCastException e) {
                logger.error("admin entry expects 2 child entries [user, pwd]");
                System.exit(1);
            }

            if (!adminUser.containsKey("user") || !adminUser.containsKey("pwd")) {
                logger.error("Username (email) and password have not been specified [user, pwd]");
                System.exit(1);
            }
            else {
                if (userService.findByEmail(adminUser.get("user")).isEmpty()) {
                    logger.info("Persisting configured initial admin user");
                    if (!isBCryptHash((String) adminUser.get("pwd"))) {
                        logger.error("The provided password must be a BCrypt hash");
                        System.exit(1);
                    }
                    User user = new User(adminUser.get("user"), UserType.ADMIN);
                    user.setPasswordHashHashed(adminUser.get("pwd"));
                    userService.save(user);
                }
            }
            return adminUser.get("user");
        }
        else {
            if (userService.findByEmail("admin").isEmpty()) {
                logger.info("Persisting default initial admin user");
                userService.save(new User(
                        "admin",
                        "admin",
                        UserType.ADMIN
                ));
            }
            return "admin";
        }

    }

    public void initKeyStores(List<String> files) {
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

    private boolean isBCryptHash(String hash) {
        // Check if the string is 60 characters long and follows the BCrypt format
        if (hash == null || hash.length() != 60) {
            return false;
        }

        // Regular expression to validate the format of the BCrypt hash
        String bcryptPattern = "^\\$2[ayb]\\$.{56}$";


        return hash.matches(bcryptPattern);
    }
}
