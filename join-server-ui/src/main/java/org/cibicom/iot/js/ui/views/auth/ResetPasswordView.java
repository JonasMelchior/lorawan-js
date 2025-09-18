package org.cibicom.iot.js.ui.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.ui.views.components.ErrorNotification;
import org.cibicom.iot.js.ui.views.components.SuccessNotification;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import java.util.Optional;

@PageTitle("Reset Password")
@Route(value = "reset-password")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {
    private String token;
    private UserService userService;
    private VerticalLayout mainLayout = new VerticalLayout();
    private Image logo;
    public ResetPasswordView(@Autowired UserService userService) {
        this.userService = userService;

        StreamResource imageResourceCibi = new StreamResource("EURECOM_logo.png",
                () -> getClass().getResourceAsStream("/icons/EURECOM_logo.png"));
        logo = new Image(imageResourceCibi, "EURECOM_logo.png");
        logo.setMaxWidth("500px");

        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        removeAll();
        Optional<String> token = beforeEnterEvent.getLocation().getQueryParameters().getSingleParameter("token");
        if (token.isEmpty()) {
            setRequestResetPasswordLayout();
        }
        else if (token.get().length() != 64) {
            new ErrorNotification("Token is not of correct length").open();
        }
        else {
            this.token = token.get();
            setResetPasswordLayout();
        }
        add(mainLayout);
    }

    private void setRequestResetPasswordLayout() {
        EmailField emailField = new EmailField("Email");
        emailField.setHelperText("After you have entered your email and submitted it, an email will be sent you. In the email, a link will be provided where you can reset your password");
        Button submitButton = new Button("Submit", click -> {
            Optional<User> user = this.userService.findByEmail(emailField.getValue());
            if (emailField.isInvalid()) {
                new ErrorNotification("Email is invalid").open();
            }
            else if (user.isEmpty()) {
                new ErrorNotification("No Join Server account is associated with the provided email").open();
            }
            else {
                try {
                    this.userService.sendResetPasswordLink(user.get());
                    new SuccessNotification("Email has been sent. Open the link in the mail to reset the password").open();
                } catch (MessagingException e) {
                    new ErrorNotification("An error occurred - email couldn't be sent").open();
                }
            }
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        FormLayout formLayout = new FormLayout();
        formLayout.add(emailField, 2);
        formLayout.add(submitButton, 2);

        mainLayout = new VerticalLayout(
                logo,
                new H3("Reset LoRaWAN® Join Server Password"),
                formLayout
        );
        mainLayout.setMaxWidth("500px");
        mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    private void setResetPasswordLayout() {
        PasswordForm passwordForm = new PasswordForm("Reset Password");
        passwordForm.persistButton.addClickListener( click -> {
            Optional<User> user = userService.findAll()
                    .stream().filter( user1 -> user1.getResetToken() != null && user1.getResetToken().equals(this.token)).findFirst();
            if (user.isEmpty()) {
                new ErrorNotification("No user could be associated with token").open();
                return;
            }


            String err = passwordForm.validate();
            if (err != null) {
                new ErrorNotification(err).open();
                return;
            }

            user.get().setPasswordHash(passwordForm.passwordField.getValue());
            userService.save(user.get());

            new SuccessNotification("Password has successfully been reset. You can now log in with your new password").open();
            UI.getCurrent().navigate(LoginView.class);
        });

        mainLayout = new VerticalLayout(
                logo,
                new H3("Reset LoRaWAN® Join Server Password"),
                passwordForm
        );
        mainLayout.setMaxWidth("500px");
        mainLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

}
