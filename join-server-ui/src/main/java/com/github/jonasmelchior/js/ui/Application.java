package com.github.jonasmelchior.js.ui;

import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.data.user.UserType;
import com.github.jonasmelchior.js.service.user.UserService;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.Security;
import java.util.Map;
import java.util.Optional;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */

@Theme(value = "join-server-ui")
@EntityScan("com.github.jonasmelchior.js.data")
@EnableJpaRepositories("com.github.jonasmelchior.js.repository")
@ComponentScan("com.github.jonasmelchior.js.service")
@Push
@EnableAsync
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class Application extends SpringBootServletInitializer implements AppShellConfigurator, CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    Logger logger = LoggerFactory.getLogger(Application.class);

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
                logger.info("Default directory couldn't be found - terminating application");
                System.exit(1);
            }
            System.setProperty("keystore_filepath_credentials", defaultPath + "/credential_keys.bcfks");
            System.setProperty("keystore_filepath_rkeys", defaultPath + "/rkeys.bcfks");
            System.setProperty("keystore_filepath_skeys", defaultPath + "/skeys.bcfks");
            System.setProperty("keystore_filepath_kek", defaultPath + "/kek.bcfks");
        }

        System.setProperty("master_pwd", "mighty_prod_pass");

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
    }
}
