package org.cibicom.iot.js.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.ui.views.admin.UsersView;
import org.cibicom.iot.js.ui.views.auth.LoginView;
import org.cibicom.iot.js.ui.views.credentials.CredentialsView;
import org.cibicom.iot.js.ui.views.device.DevicesView;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */

@Uses(DevicesView.class)
@Uses(CredentialsView.class)
@Uses(UsersView.class)
@Uses(Avatar.class)
@Uses(MenuBar.class)
public class MainLayout extends AppLayout implements AfterNavigationObserver {
    private H2 viewTitle;
    Header header = new Header();

    public MainLayout() {
        viewTitle = new H2();

        addToNavbar(createHeaderContent());
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Width.FULL);

        Div layout = new Div();
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.Horizontal.LARGE);

        Image cibiLogo = new Image("icons/EURECOM_icon.png", "placeholder app icon");
        cibiLogo.setWidth("80px");
        cibiLogo.setHeight("50px");

        H3 appName = new H3("LoRaWAN® Join Server");
        HorizontalLayout cibiHeader = new HorizontalLayout(cibiLogo, appName);
        cibiHeader.addClassNames(LumoUtility.Margin.Vertical.MEDIUM, LumoUtility.Margin.End.AUTO, LumoUtility.FontSize.LARGE);
        cibiHeader.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.add(cibiHeader);

        cibiHeader.setWidthFull();
        cibiHeader.setFlexGrow(1, appName);
        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user!= null) {
            Avatar avatar = new Avatar(user.getFirstName() + " " + user.getLastName());
            avatar.setColorIndex(5);
            avatar.addThemeVariants(AvatarVariant.LUMO_LARGE);
            MenuBar menuBar = new MenuBar();
            menuBar.setOpenOnHover(true);
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
            menuBar.addThemeVariants(MenuBarVariant.LUMO_LARGE);

            MenuItem menuItem = menuBar.addItem(avatar);
            SubMenu subMenu = menuItem.getSubMenu();
            subMenu.addItem("Account Details", menuItemClickEvent -> createAccountDetailsDialog().open());
            subMenu.addItem("Sign out", menuItemClickEvent -> {
                VaadinSession.getCurrent().getSession().setAttribute("SPRING_SECURITY_CONTEXT", null);
                VaadinSession.getCurrent().setAttribute(User.class, null);
                UI.getCurrent().navigate(LoginView.class);
            });

            cibiHeader.add(menuBar);

        }

        Nav nav = new Nav();
        nav.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Overflow.AUTO, LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.XSMALL);

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL, LumoUtility.ListStyleType.NONE, LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            list.add(menuItem);

        }

        header.add(layout, nav);
        return header;
    }

    private MenuItemInfo[] createMenuItems() {

        User user = VaadinSession.getCurrent().getAttribute(User.class);

        if (user != null) {
            if (user.getUserType() == UserType.ADMIN) {
                return new MenuItemInfo[]{ //
                        new MenuItemInfo("Devices", LineAwesomeIcon.SQUARE.create(), DevicesView.class), //
                        new MenuItemInfo("Credentials", VaadinIcon.KEY.create(), CredentialsView.class),
                        new MenuItemInfo("Users", VaadinIcon.USERS.create(), UsersView.class)
                };
            }
            else {
                return new MenuItemInfo[]{ //
                        new MenuItemInfo("Devices", LineAwesomeIcon.SQUARE.create(), DevicesView.class), //
                        new MenuItemInfo("Credentials", VaadinIcon.KEY.create(), CredentialsView.class)
                };
            }
        }

        return null;
    }

    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.XSMALL, LumoUtility.Height.MEDIUM, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.Horizontal.SMALL,
                    LumoUtility.TextColor.BODY);
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames(LumoUtility.FontWeight.MEDIUM, LumoUtility.FontSize.MEDIUM, LumoUtility.Whitespace.NOWRAP);

            if (icon != null) {
                link.add(icon);
            }
            link.add(text);
            add(link);
        }

        public Class<?> getView() {
            return view;
        }

    }

    private Dialog createAccountDetailsDialog() {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Account Details");
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(closeButton);

        TextField emailField = new TextField("Email");
        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        TextField organizationField = new TextField("Organization");

        emailField.setReadOnly(true);
        firstNameField.setReadOnly(true);
        lastNameField.setReadOnly(true);
        organizationField.setReadOnly(true);

        FormLayout formLayout = new FormLayout(firstNameField, lastNameField, emailField, organizationField);

        User user = VaadinSession.getCurrent().getAttribute(User.class);
        if (user != null) {
            if (user.getFirstName() != null) {
                firstNameField.setValue(user.getFirstName());
            }
            if (user.getLastName() != null) {
                lastNameField.setValue(user.getLastName());
            }
            if (user.getEmail() != null) {
                emailField.setValue(user.getEmail());
            }
            if (user.getOrganization() != null) {
                organizationField.setValue(user.getOrganization());
            }
        }

        dialog.add(formLayout);

        return dialog;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        String title = (String) VaadinSession.getCurrent().getAttribute("title");
        return title == null ? "" : title;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }
}
