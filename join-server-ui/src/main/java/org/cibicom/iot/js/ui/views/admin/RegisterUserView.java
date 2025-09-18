package org.cibicom.iot.js.ui.views.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.ui.views.MainLayout;
import org.cibicom.iot.js.ui.views.components.ErrorNotification;
import org.cibicom.iot.js.ui.views.components.SuccessNotification;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;

@Route(value = "users/register", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class RegisterUserView extends VerticalLayout {
    FormLayout formLayout = new FormLayout();
    EmailField emailField = new EmailField("Email");
    TextField firstNameField = new TextField("First Name");
    TextField lastNameField = new TextField("Last Name");
    TextField organizationField = new TextField("Organization");
    ComboBox<UserType> userTypeComboBox = new ComboBox<>("Role");
    Button registerUserButton = new Button("Invite User", new Icon(VaadinIcon.PLUS));

    public RegisterUserView(@Autowired UserService userService) {
        formLayout.add(emailField, 2);
        formLayout.add(firstNameField, lastNameField);
        formLayout.add(organizationField, 2);
        formLayout.add(userTypeComboBox, 2);
        formLayout.add(registerUserButton, 2);

        userTypeComboBox.setItems(UserType.values());

        registerUserButton.addClickListener( click -> {
            User user = new User(
                    emailField.getValue(),
                    firstNameField.getValue(),
                    lastNameField.getValue(),
                    organizationField.getValue()
            );

            user.setUserType(userTypeComboBox.getValue());

            try {
                userService.inviteUser(user);
                new SuccessNotification("Invitation email sent to user " + user.getEmail()).open();
            } catch (MessagingException e) {
                new ErrorNotification("An error occurred when sending the invitation email:\n" + e.getMessage()).open();
            }

            UI.getCurrent().navigate(UsersView.class);
        });
        registerUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(formLayout);
    }

}
