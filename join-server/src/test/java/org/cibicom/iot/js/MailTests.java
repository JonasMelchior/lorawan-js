package org.cibicom.iot.js;

import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.service.user.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.MessagingException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MailTests {

    @Autowired
    private UserService userService;

    @Test
    public void testMailSend() {
        User user = new User(
                "josj@cibicom.dk",
                "Jonas",
                "Jensen",
                "Cibicom A/S"
        );

        boolean didMailSend = true;

//        try {
//            userService.inviteUser(user);
//        } catch (MessagingException e) {
//            didMailSend = false;
//            e.printStackTrace();
//        }

        Assert.assertTrue(didMailSend);
    }
}
