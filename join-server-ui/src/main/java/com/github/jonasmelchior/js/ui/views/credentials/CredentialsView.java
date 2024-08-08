package com.github.jonasmelchior.js.ui.views.credentials;

import com.github.jonasmelchior.js.ui.views.MainLayout;
import com.github.jonasmelchior.js.ui.views.components.UnauthenticatedNotification;
import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.user.UserService;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@PageTitle("Credentials")
@Route(value = "credentials", layout = MainLayout.class)
public class CredentialsView extends HorizontalLayout {

    private KeyCredentialService keyCredentialService;
    private DeviceKeyHandler deviceKeyHandler;
    private UserService userService;

    VerticalLayout mainLayout;

    public CredentialsView(@Autowired DeviceService deviceService,
                           @Autowired KeyCredentialService keyCredentialService,
                           @Autowired DevKeyIdService devKeyIdService,
                           @Autowired UserService userService,
                           @Autowired JoinLogService joinLogService,
                           @Autowired RunningJobService runningJobService) {
        this.keyCredentialService = keyCredentialService;
        this.deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devKeyIdService
                ),
                joinLogService,
                runningJobService
        );
        this.userService = userService;

        setMainLayout();

        add(mainLayout);
        setSizeFull();
    }

    @Transactional
    public void setMainLayout() {
        Grid<KeyCredential> keyCredentialGrid = new Grid<>();

        keyCredentialGrid.addColumn(KeyCredential::getIdentifier).setHeader("Identifier");
        //keyCredentialGrid.addColumn(keyCredential -> keyCredential.getDevIds().size()).setHeader("Covering Devices");
        keyCredentialGrid.addComponentColumn( keyCredential -> new Button(new Icon(VaadinIcon.EYE), click -> seeCredentialDialog(keyCredential).open())).setHeader("See Credential");

        keyCredentialGrid.setItems(keyCredentialService.findByOwner(VaadinSession.getCurrent().getAttribute(User.class)));

        mainLayout = new VerticalLayout(
                keyCredentialGrid
        );
    }

    private Dialog seeCredentialDialog(KeyCredential keyCredential) {
        Dialog dialog = new Dialog();

        VerticalLayout dialogLayout = new VerticalLayout(new H2("Credential for " + keyCredential.getIdentifier()));

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRequired(true);
        passwordField.setHelperText("Enter password for account");

        Button confirmButton = new Button("Confirm", click -> {
            Optional<User> user = userService.authorize(VaadinSession.getCurrent().getAttribute(User.class).getEmail(), passwordField.getValue());
            if (user.isEmpty()) {
                new UnauthenticatedNotification().open();
            }
            else {
                dialogLayout.add(new Text("Credential: " + deviceKeyHandler.getCredential(keyCredential.getIdentifier())));
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialogLayout.add(passwordField, confirmButton);

        dialog.add(dialogLayout);

        return dialog;
    }
}
