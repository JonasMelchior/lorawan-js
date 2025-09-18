package org.cibicom.iot.js.ui.views.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.cibicom.iot.js.auth.util.JwtUtil;
import org.cibicom.iot.js.data.auth.LoginRes;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.ui.sec.SecurityService;
import org.cibicom.iot.js.ui.views.components.Divider;
import org.cibicom.iot.js.ui.views.components.ErrorNotification;
import org.cibicom.iot.js.ui.views.device.DevicesView;
import org.cibicom.iot.js.ui.views.err.CustomErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@PageTitle("Log In")
@Route(value = "login")
public class LoginView extends VerticalLayout{
    private FormLayout formLayout = new FormLayout();
    EmailField emailField = new EmailField("Email");
    PasswordField passwordField = new PasswordField("Password");
    Anchor resetPasswordLink = new Anchor();
    @Value("${url.prefix}")
    private String urlPrefix;
    public LoginView(@Autowired UserService userService, @Autowired SecurityService securityService, @Autowired JwtUtil jwtUtil) {
        emailField.setRequired(true);
        passwordField.setRequired(true);
        passwordField.setPattern("^(?=(.*[a-z]))(?=(.*[A-Z]))(?=(.*\\d))(?=(.*[!@#$%^&*()_+{}\\[\\]:;\"'<>,.?/-]))[A-Za-z\\d!@#$%^&*()_+{}\\[\\]:;\"'<>,.?/-]{8,}$");

        // Login button action
        Button loginButton = new Button("Login", click -> {
            try {
                LoginRes loginResult = securityService.authenticate(emailField.getValue(), passwordField.getValue());
                Claims claims = jwtUtil.resolveClaims(loginResult.getToken());

                String email = claims.getSubject();
                String roles = claims.get("roles", String.class);
                List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                Optional<User> user = userService.findByEmail(email);
                if (user.isEmpty()) {
                    new ErrorNotification("Login Failed").open();
                }
                else {
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(email, "", authorities);
                    var context = SecurityContextHolder.getContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    VaadinSession.getCurrent().getSession().setAttribute("SPRING_SECURITY_CONTEXT", context);
                    VaadinSession.getCurrent().setAttribute(User.class, user.get());
                    VaadinSession.getCurrent().setErrorHandler(new CustomErrorHandler());

                    UI.getCurrent().navigate(DevicesView.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
                new ErrorNotification("Login Failed").open();
            }
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Title
        Div title = new Div();
        title.setText("Join");
        title.getElement().setProperty("innerHTML", "LoRaWAN® Join Server"); // Line break
        title.getStyle().set("font-size", "3em");
        title.getStyle().set("font-weight", "bold");
        title.getStyle().set("line-height", "1.2");
        title.getStyle().set("margin", "auto 0");

        // Logo
        StreamResource imageResourceCibi = new StreamResource("EURECOM_logo.png",
                () -> getClass().getResourceAsStream("/icons/EURECOM_logo.png"));
        Image cibiLogo = new Image(imageResourceCibi, "EURECOM_logo.png");

        StreamResource imageResourceSecServer = new StreamResource("security_server.png",
                () -> getClass().getResourceAsStream("/icons/security_server.png"));
        Image secServer = new Image(imageResourceSecServer, "security_server");

        // Scale the image down by setting its width and height
        secServer.setMaxWidth("200px"); // Set desired width (e.g., 200px)
        secServer.setMaxHeight("200px"); // Set desired height (e.g., 200px)

        VerticalLayout titleLayout = new VerticalLayout(
                secServer,
                title
        );

        // Form layout
        formLayout.add(cibiLogo);
        formLayout.add(emailField, 2);
        formLayout.add(passwordField, 2);
        formLayout.add(loginButton, 2);
        formLayout.setSizeUndefined();
        formLayout.add(resetPasswordLink);

        // Create vertical login layout
        VerticalLayout loginLayout = new VerticalLayout(formLayout);
        loginLayout.setMaxWidth("500px");
        loginLayout.setAlignItems(Alignment.CENTER); // Center the content inside

        // Main layout (HorizontalLayout)
        HorizontalLayout layout = new HorizontalLayout(
                loginLayout,
                new Divider(),
                titleLayout
        );
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER); // Center vertically in the HorizontalLayout
        layout.setSizeFull(); // Ensure the layout fills the screen size
        layout.setAlignItems(Alignment.CENTER); // Center horizontally and vertically in the layout

        // Add the layout to the root view
        add(layout);
        setSizeFull(); // Make sure the root VerticalLayout takes up full height and width
        setDefaultHorizontalComponentAlignment(Alignment.CENTER); // Center the root layout's content
    }

    @PostConstruct
    private void setResetPasswordLink() {
        resetPasswordLink.setHref(this.urlPrefix + "reset-password");
        resetPasswordLink.setText("Reset Password");
        resetPasswordLink.getElement().addEventListener("click", e -> {
            UI.getCurrent().navigate(resetPasswordLink.getHref());
        });
        resetPasswordLink.getStyle().set("color", "orange");
    }
}
