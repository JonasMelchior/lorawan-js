package com.github.jonasmelchior.js.ui.views.auth;

import com.github.jonasmelchior.js.ui.views.components.ErrorNotification;
import com.github.jonasmelchior.js.ui.views.components.UnauthenticatedNotification;
import com.github.jonasmelchior.js.ui.views.device.DevicesView;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.service.user.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@PageTitle("Log In")
@Route(value = "login")
@RouteAlias(value = "")
public class LoginView extends VerticalLayout {
    private FormLayout layout = new FormLayout();
    private UserService userService;


    public LoginView(@Autowired UserService userService) {
        this.userService = userService;

        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Password");
        emailField.setRequired(true);
        passwordField.setRequired(true);

        Button loginButton = new Button("Login", click -> {
            if (emailField.isEmpty() || passwordField.isEmpty()) {
                new ErrorNotification("You must fill out the required fields").open();
            }
            else {
                Optional<User> user = userService.authorize(emailField.getValue(), passwordField.getValue());
                if (user.isEmpty()) {
                    new UnauthenticatedNotification().open();
                }
                else {
                    VaadinSession.getCurrent().setAttribute(User.class, user.get());
                    UI.getCurrent().navigate(DevicesView.class);
                }
            }
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(emailField, 2);
        layout.add(passwordField, 2);
        layout.add(loginButton, 2);

        add(layout);
        setMaxWidth("500px");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }
}
