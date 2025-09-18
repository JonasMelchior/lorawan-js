package org.cibicom.iot.js.ui.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

import java.util.Optional;

@PageTitle("Activate Account")
@Route(value = "activate-account")
@AnonymousAllowed
public class ActivateView extends VerticalLayout implements BeforeEnterObserver {
    private String token;
    public ActivateView(@Autowired UserService userService) {
        PasswordForm passwordForm = new PasswordForm("Activate Account");
        passwordForm.persistButton.addClickListener( click -> {
            Optional<User> user = userService.findAll()
                    .stream().filter( user1 -> user1.getActivationToken() != null && user1.getActivationToken().equals(this.token)).findFirst();
            if (user.isEmpty()) {
                new ErrorNotification("No user could be associated with token").open();
                return;
            }
            else if (user.get().getActive()) {
                new ErrorNotification("User has already been activated").open();
                return;
            }

            String err = passwordForm.validate();
            if (err != null) {
                new ErrorNotification(err).open();
                return;
            }

            user.get().setPasswordHash(passwordForm.passwordField.getValue());
            user.get().setActive(true);
            userService.save(user.get());

            new SuccessNotification("Account successfully activated. You can now log in with your new password").open();
            UI.getCurrent().navigate(LoginView.class);
        });

        StreamResource imageResourceCibi = new StreamResource("EURECOM_logo.png",
                () -> getClass().getResourceAsStream("/icons/EURECOM_logo.png"));
        Image cibiLogo = new Image(imageResourceCibi, "EURECOM_logo.png");

        cibiLogo.setMaxWidth("500px");

        VerticalLayout activateLayout = new VerticalLayout(
                cibiLogo,
                new H3("Activate LoRaWAN® Join Server Account"),
                passwordForm
        );

        activateLayout.setMaxWidth("500px");
        activateLayout.setAlignItems(Alignment.CENTER); // Center the content inside

        add(activateLayout);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<String> token = beforeEnterEvent.getLocation().getQueryParameters().getSingleParameter("token");
        if (token.isEmpty()) {
            new ErrorNotification("Token not present in URL query parameter").open();
        }
        else if (token.get().length() != 64) {
            new ErrorNotification("Token is not of correct length").open();
        }
        else {
            this.token = token.get();
        }
    }
}
