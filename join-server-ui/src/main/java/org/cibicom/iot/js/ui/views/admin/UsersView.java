package org.cibicom.iot.js.ui.views.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.ui.views.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "users", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class UsersView extends VerticalLayout {
    UserService userService;
    Grid<User> userGrid = new Grid<>();
    Editor<User> editor = userGrid.getEditor();
    public UsersView(@Autowired UserService userService) {
        this.userService = userService;

        Grid.Column<User> emailColumn = userGrid.addColumn(User::getEmail).setHeader("Email");
        Grid.Column<User> firstNameColumn = userGrid.addColumn(User::getFirstName).setHeader("First Name");
        Grid.Column<User> lastNameColumn = userGrid.addColumn(User::getLastName).setHeader("Last Name");
        Grid.Column<User> organizationColumn = userGrid.addColumn(User::getOrganization).setHeader("Organization");
        Grid.Column<User> userTypeColumn = userGrid.addColumn(User::getUserType).setHeader("Role");
        userGrid.addComponentColumn( user -> {
            Checkbox activatedCheckbox = new Checkbox();
            activatedCheckbox.setReadOnly(true);
            if (user.getActive() != null) {
                activatedCheckbox.setValue(user.getActive());
            }
            return activatedCheckbox;
        }).setHeader("Active");
        Grid.Column<User> editColumn = userGrid.addComponentColumn(user -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(e -> {
                if (editor.isOpen())
                    editor.cancel();
                userGrid.getEditor().editItem(user);
            });

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH), click ->  {
                deleteUserDialog(user).open();
            });

            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            return new HorizontalLayout(editButton, deleteButton);
        }).setWidth("150px").setFlexGrow(0);

        Binder<User> binder = new Binder<>(User.class);
        editor.setBinder(binder);
        editor.setBuffered(true);
        editor.addSaveListener( updatedUser -> {
            userService.save(updatedUser.getItem());
        });

        TextField emailField = new TextField();
        emailField.setWidthFull();
        binder.forField(emailField).asRequired("Email must not be empty")
                .bind(User::getEmail, User::setEmail);
        emailColumn.setEditorComponent(emailField);

        TextField firstNameField = new TextField();
        firstNameField.setWidthFull();
        binder.forField(firstNameField).bind(User::getFirstName, User::setFirstName);
        firstNameColumn.setEditorComponent(firstNameField);

        TextField lastNameField = new TextField();
        lastNameField.setWidthFull();
        binder.forField(lastNameField).bind(User::getLastName, User::setLastName);
        lastNameColumn.setEditorComponent(lastNameField);

        TextField organizationField = new TextField();
        organizationField.setWidthFull();
        binder.forField(organizationField)
                .asRequired("Organization must not be empty")
                .bind(User::getOrganization, User::setOrganization);
        organizationColumn.setEditorComponent(organizationField);

        ComboBox<UserType> userTypeComboBox = new ComboBox<>();
        userTypeComboBox.setItems(UserType.values());
        userTypeComboBox.setWidthFull();
        binder.forField(userTypeComboBox)
                .asRequired("User Role must not be empty")
                .bind(User::getUserType, User::setUserType);
        userTypeColumn.setEditorComponent(userTypeComboBox);

        Button saveButton = new Button("Save", e -> editor.save());
        Button cancelButton = new Button(VaadinIcon.CLOSE.create(),
                e -> editor.cancel());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_ERROR);
        HorizontalLayout actions = new HorizontalLayout(saveButton,
                cancelButton);
        actions.setPadding(false);
        editColumn.setEditorComponent(actions);

        Button addUserButton = new Button("Invite User", new Icon(VaadinIcon.PLUS));
        addUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserButton.addClickListener( click -> UI.getCurrent().navigate(RegisterUserView.class));

        userGrid.setItems(userService.findAll());

        setHeightFull();
        add(addUserButton, userGrid);
    }

    private ConfirmDialog deleteUserDialog(User user) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete User " + user.getEmail());
        confirmDialog.setText("Are you sure you want to delete this user? " +
                "After deletion has occurred, it cannot be undone. All devices owned by this user will also be marked for deletion after 24 hours.");
        confirmDialog.setCancelable(true);
        confirmDialog.addConfirmListener( confirm -> {
            userService.delete(user);
            userGrid.setItems(userService.findAll());
        });

        return confirmDialog;
    }
}
