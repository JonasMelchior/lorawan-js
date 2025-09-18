package org.cibicom.iot.js.ui;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.LoadDependenciesOnStartup;
import com.vaadin.flow.theme.Theme;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.service.utils.init.JsInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import java.security.Security;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */


@Theme(value = "join-server-ui")
@EntityScan("org.cibicom.iot.js.data")
@EnableJpaRepositories("org.cibicom.iot.js.repository")
@ComponentScan({"org.cibicom.iot.js.ui.sec", "org.cibicom.iot.js.service", "org.cibicom.iot.js.auth.util"})
@Push
@EnableAsync
@SpringBootApplication()
@LoadDependenciesOnStartup
public class UI extends SpringBootServletInitializer implements AppShellConfigurator, CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(UI.class, args);
    }

    Logger logger = LoggerFactory.getLogger(UI.class);

    @Override
    public void run(String... args) throws Exception {
        Security.addProvider(new BouncyCastleFipsProvider());

        // User Service not needed, because initial admin user is initialized in the Join Server Service
        JsInit jsInit = new JsInit(null);

        jsInit.setMasterPassword();
        jsInit.setKeystorePaths();
    }
}
