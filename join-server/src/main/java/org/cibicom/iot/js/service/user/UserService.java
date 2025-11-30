package org.cibicom.iot.js.service.user;

import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Service
public class UserService implements IUserService, UserDetailsService {

    @Autowired
    private UserRepository repository;
    @Value("${url.prefix}")
    private String urlPrefix;
    @Override
    public List<User> findAll() {
        return repository.findAll();
    }
    public static String passwordRegex = "^(?=(.*[a-z]))(?=(.*[A-Z]))(?=(.*\\d))(?=(.*[!@#$%^&*()_+{}\\[\\]:;\"'<>,.?/-]))[A-Za-z\\d!@#$%^&*()_+{}\\[\\]:;\"'<>,.?/-]{8,}$";
    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findUserByEmail(email);
    }
    @Value("${app.smtp.config.host}")
    private String host;

    @Value("${app.smtp.config.port}")
    private String port;

    @Value("${app.smtp.config.tls}")
    private String tlsEnabled;

    @Value("${app.smtp.config.auth}")
    private String authEnabled;

    @Value("${app.smtp.config.user.from}")
    private String emailFrom;

    @Value("${app.smtp.config.user}")
    private String smtpUser;

    @Value("${app.smtp.config.password}")
    private String smtpPwd;

    @Value("${domain.name}")
    private String domainName;

    @Value("${organization.name}")
    private String organizationName;


    public void inviteUser(User user) throws MessagingException {
        String token = generateSecureRandomToken();

        String subject = "Invitation " + domainName;
        String messageBody = "Hello " + user.getFirstName() + " " + user.getLastName()
                + "\nYou have been invited to create an account at " + organizationName +  " LoRaWAN® Join Server."
                + "\nTo create an account, please follow the link below:\n"
                + urlPrefix + "activate-account?token=" + token;

        sendMail(user.getEmail(), subject, messageBody);

        user.setActivationToken(token);
        user.setActive(false);

        repository.save(user);
    }

    public void sendResetPasswordLink(User user) throws MessagingException {
        String token = generateSecureRandomToken();

        String subject = "Reset password request " + System.getProperty("domain.name");
        String messageBody = "Hello " + user.getFirstName() + " " + user.getLastName()
                + "\nYou have requested to reset your password."
                + "\nIn case the request didn't come from you or was a mistake, please ignore this message."
                + "\nOtherwise, open the link below:\n"
                + urlPrefix + "reset-password?token=" + token;

        sendMail(user.getEmail(), subject, messageBody);

        user.setResetToken(token);

        repository.save(user);
    }

    private void sendMail(String receiver, String subject, String messageBody) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", authEnabled);
        properties.put("mail.smtp.starttls.enable", tlsEnabled);

        Session session;

        if (authEnabled.equals("false")) {
            session = Session.getInstance(properties);
        }
        else {
            session = Session.getInstance(properties, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPwd);
                }
            });
        }

        // Create a new email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailFrom));  // Set a valid from address
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
        message.setSubject(subject);
        message.setText(messageBody);

        // Send the email
        Transport.send(message);
    }

    private String generateSecureRandomToken () {
        SecureRandom random = new SecureRandom();
        StringBuilder hexString = new StringBuilder(64);

        for (int i = 0; i < 64; i++) {
            hexString.append(Integer.toHexString(random.nextInt(16)));
        }

        return hexString.toString();
    }

    @Override
    public Optional<User> authorize(String email, String password) {
        Optional<User> user = findByEmail(email);
        if (user.isPresent()) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

            if (bCryptPasswordEncoder.matches(password, user.get().getPasswordHash())) {
                return user;
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) {
        repository.save(user);
    }

    @Override
    public void delete(User user) {
        repository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = repository.findUserByEmail(email);
        if (user.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }
        else {
            List<String> roles = new ArrayList<>();
            roles.add("USER");
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.get().getEmail())
                    .password(user.get().getPasswordHash())
                    .roles(roles.toArray(new String[0]))
                    .build();
        }

    }
}
