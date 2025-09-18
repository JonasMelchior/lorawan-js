package org.cibicom.iot.js.ui.views.auth;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import org.cibicom.iot.js.service.user.UserService;

public class PasswordForm extends FormLayout {
    PasswordField passwordField = new PasswordField("Enter Password");
    PasswordField repeatPasswordField = new PasswordField("Repeat Password");
    Button persistButton = new Button();

    public PasswordForm(String buttonLabel) {
        persistButton.setText(buttonLabel);
        persistButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        passwordField.setRequired(true);
        passwordField.setPattern(UserService.passwordRegex);
        passwordField.setHelperText("A password must contain at least 8 characters, 1 number, 1 upper and lower-case character, and one special character");
        repeatPasswordField.setRequired(true);

        add(passwordField, 2);
        add(repeatPasswordField, 2);
        add(persistButton, 2);
    }

    public String validate() {
        if (passwordField.isEmpty() || repeatPasswordField.isEmpty()) {
            return "You must fill out both password fields";
        }
        else if (!passwordField.getValue().equals(repeatPasswordField.getValue())) {
            return "Passwords don't match";
        }
        else if (!passwordField.getValue().matches(UserService.passwordRegex)) {
            return "Password requirements have not been met";
        }

        return null;
    }
}
