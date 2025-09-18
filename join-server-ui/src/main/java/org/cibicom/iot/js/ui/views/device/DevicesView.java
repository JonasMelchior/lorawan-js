package org.cibicom.iot.js.ui.views.device;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.codec.binary.Hex;
import org.cibicom.iot.js.data.device.Device;
import org.cibicom.iot.js.data.device.spec.DeviceSpecification;
import org.cibicom.iot.js.data.job.RunningJob;
import org.cibicom.iot.js.data.keys.KeyCredential;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.service.device.DevKeyIdService;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.device.keys.DeviceKeyHandler;
import org.cibicom.iot.js.service.device.keys.KeyHandler;
import org.cibicom.iot.js.service.log.AppSKeyReqLogService;
import org.cibicom.iot.js.service.log.JoinLogService;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.service.utils.RunningJobService;
import org.cibicom.iot.js.ui.views.MainLayout;
import org.cibicom.iot.js.ui.views.components.Pagination;
import org.cibicom.iot.js.ui.views.components.SuccessNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "devices", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "USER"})
public class DevicesView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {
    private DeviceService deviceService;
    private DeviceKeyHandler deviceKeyHandler;
    private DevKeyIdService devKeyIdService;
    private KeyCredentialService keyCredentialService;
    private JoinLogService joinLogService;
    private AppSKeyReqLogService appSKeyReqLogService;
    private RunningJobService runningJobService;
    VerticalLayout mainLayout = new VerticalLayout();
    private String title = "";
    Grid<Device> deviceGrid;
    Pagination paginationControls = new Pagination();
    TextField searchField = new TextField();
    @Value("${url.prefix}")
    private String urlPrefix;
    String parameter = "";

    public DevicesView(@Autowired UserService userService,
                       @Autowired DeviceService deviceService,
                       @Autowired KeyCredentialService keyCredentialService,
                       @Autowired DevKeyIdService devKeyIdService,
                       @Autowired JoinLogService joinLogService,
                       @Autowired AppSKeyReqLogService appSKeyReqLogService,
                       @Autowired RunningJobService runningJobService
                       ) {
        this.deviceService = deviceService;
        this.devKeyIdService = devKeyIdService;
        this.keyCredentialService = keyCredentialService;
        this.joinLogService = joinLogService;
        this.appSKeyReqLogService = appSKeyReqLogService;
        this.deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devKeyIdService
                ),
                joinLogService,
                runningJobService,
                appSKeyReqLogService
                );
        this.runningJobService = runningJobService;

        add(mainLayout);
        mainLayout.setSizeFull();
        setSizeFull();
    }

    @Override
    public String getPageTitle() {
        return this.title;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @WildcardParameter String parameter) {
        mainLayout.removeAll();

        this.parameter = parameter;
        if (parameter.isEmpty()) {
            setDevicesLayout();
        }
        else if (parameter.equals("register")){
            setRegisterLayout();
        }
        else if (deviceService.findByDevEUI(parameter).isPresent()) {
            setDeviceLayout(deviceService.findByDevEUI(parameter).get());
        }
    }

    private void setRegisterLayout() {
        title = "Register Device";
        VaadinSession.getCurrent().setAttribute("title", title);

        mainLayout.add(new RegisterDevicesView(
                this.deviceKeyHandler,
                this.keyCredentialService,
                deviceService
        ));
    }

    private void setDeviceLayout(Device device) {
        title = "Devices | " + device.getDevEUI();
        VaadinSession.getCurrent().setAttribute("title", title);

        mainLayout.add(new DeviceView(
                device,
                deviceService,
                deviceKeyHandler,
                joinLogService,
                appSKeyReqLogService,
                devKeyIdService
        ));
    }

    private void setDevicesLayout() {
        title = "Devices";
        VaadinSession.getCurrent().setAttribute("title", title);

        deviceGrid = new Grid<>();
        deviceGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        deviceGrid.addComponentColumn( device -> {
            Anchor deviceLink = new Anchor(this.urlPrefix + "devices/" + device.getDevEUI());
            deviceLink.setText(device.getDevEUI());
            deviceLink.getElement().addEventListener("click", e -> {
                UI.getCurrent().navigate(deviceLink.getHref());
            });
            deviceLink.getStyle().set("color", "orange");
            return deviceLink;
        }).setHeader("DevEUI");
        deviceGrid.addComponentColumn( device -> {
            switch (device.getMacVersion()) {
                case LORAWAN_1_0 -> {
                    return new Text("1.0");
                }
                case LORAWAN_1_0_1 -> {
                    return new Text("1.0.1");
                }
                case LORAWAN_1_0_2 -> {
                    return new Text("1.0.2");
                }
                case LORAWAN_1_0_3 -> {
                    return new Text("1.0.3");
                }
                case LORAWAN_1_0_4 -> {
                    return new Text("1.0.4");
                }
                case LORAWAN_1_1 -> {
                    return new Text("1.1");
                }
                default -> {
                    return new Text("N/A");
                }
            }
        }).setHeader("LoRaWAN Version");
        deviceGrid.addComponentColumn( device -> {
            switch (device.getSessionStatus().getState()) {
                case INIT -> {
                    return new Text("Initialized");
                }
                case ACTIVE -> {
                    return new Text("Active");
                }
                case EOL -> {
                    return new Text("EOL");
                }
                default -> {
                    return new Text("N/A");
                }
            }
        }).setHeader("Session Status");
        deviceGrid.addComponentColumn(device -> {
            if (device.getLastJoin() != null) {
                return new Text(device.getLastJoin().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss")));
            }
            else {
                return new Text("N/A");
            }
        }).setHeader("Last Join");
        deviceGrid.addComponentColumn( device -> {
            Icon icon;
            if (device.getRootKeysExposed()) {
                icon = new Icon(VaadinIcon.MINUS);
                icon.setColor("red");
            }
            else {
                icon = new Icon(VaadinIcon.CHECK);
                icon.setColor("green");
            }
            return icon;
        }).setHeader("Root Keys Concealed");

        Button deleteSelectedDevicesButton = new Button("Delete selected devices", new Icon(VaadinIcon.TRASH));
        deleteSelectedDevicesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteSelectedDevicesButton.addClickListener( click -> {
            deleteSelectedDevicesDialog(deviceGrid.getSelectedItems().stream().toList()).open();
        });

        HorizontalLayout selectedDevicesActionLayout = new HorizontalLayout(deleteSelectedDevicesButton);
        selectedDevicesActionLayout.setVisible(false);
        selectedDevicesActionLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        deviceGrid.addSelectionListener( selection -> selectedDevicesActionLayout.setVisible(!selection.getAllSelectedItems().isEmpty()));

        searchField.setPlaceholder("Search");
        searchField.setWidth("300px");
        searchField.setSuffixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        Page<Device> devicePageInitial = deviceService.getPage(
                PageRequest.of(0, 20, Sort.by("lastJoin")),
                DeviceSpecification.isOwnedBy(VaadinSession.getCurrent().getAttribute(User.class))
                //DeviceSpecification.isOwnedBy(VaadinSession.getCurrent().getAttribute(User.class))
        );

        deviceGrid.setItems(devicePageInitial.stream().toList());
        paginationControls.setMaxPages(devicePageInitial.getTotalPages());
        paginationControls.addPageChangeListener( change -> {
            if (!searchField.isEmpty()) {
                Page<Device> devicePageDevContainsDevEUI = deviceService.getPage(
                        PageRequest.of(change.getNewPageCounterValue() - 1, 20, Sort.by("lastJoin")),
                        DeviceSpecification.isOwnedByAndContainsEui(VaadinSession.getCurrent().getAttribute(User.class), searchField.getValue())
                );
                deviceGrid.setItems(devicePageDevContainsDevEUI.stream().toList());
            }
            else {
                Page<Device> devicePage = deviceService.getPage(
                        PageRequest.of(change.getNewPageCounterValue() - 1, 20, Sort.by("lastJoin")),
                        DeviceSpecification.isOwnedBy(VaadinSession.getCurrent().getAttribute(User.class))
                );
                deviceGrid.setItems(devicePage.stream().toList());
            }
        });

        searchField.addValueChangeListener( change -> {
            paginationControls.setPageCounter(1);
            if (!change.getValue().isEmpty()) {
                Page<Device> pageDevContainsDevEUI = deviceService.getPage(
                        PageRequest.of(0, 20, Sort.by("lastJoin")),
                        DeviceSpecification.isOwnedByAndContainsEui(VaadinSession.getCurrent().getAttribute(User.class), searchField.getValue())
                );
                deviceGrid.setItems(pageDevContainsDevEUI.stream().toList());
                paginationControls.setMaxPages(pageDevContainsDevEUI.getTotalPages());
            }
            else {
                deviceGrid.setItems(devicePageInitial.stream().toList());
                paginationControls.setMaxPages(devicePageInitial.getTotalPages());
            }
        });

        Button registerButton = new Button("Register Device(s)", new Icon(VaadinIcon.PLUS));
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener( click -> {
            UI.getCurrent().navigate(this.urlPrefix + "devices/register");
        });

        ProgressBar progressBar = new ProgressBar();
        progressBar.setWidth("15em");
        progressBar.setIndeterminate(true);

        VerticalLayout layout = new VerticalLayout();

        //TODO : implement
        List<RunningJob> runningJobs = runningJobService.findByOwner(VaadinSession.getCurrent().getAttribute(User.class));
        //List<RunningJob> runningJobs = runningJobService.findByOwner(VaadinSession.getCurrent().getAttribute(User.class));
        if (!runningJobs.isEmpty()) {
            progressBar.setVisible(true);
            layout.add(new H4("Tasks in background:"));
            for (RunningJob runningJob : runningJobs) {
                layout.add(new Span("Securely storing keys for " + runningJob.getDevicesNum() + " devices..."));
            }
            layout.add(progressBar);
        }

        layout.add(
                registerButton,
                searchField,
                selectedDevicesActionLayout,
                deviceGrid,
                paginationControls
        );

        layout.setHeightFull();

        mainLayout.add(layout);
    }

    private Dialog deleteSelectedDevicesDialog(List<Device> devices) {
        Dialog dialog = new Dialog();

        VerticalLayout verticalLayout = new VerticalLayout(
                new H2("Delete Device"),
                new H4("Are you sure you want to delete " + devices.size() + " devices?")
        );

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        Button saveButton = new Button("Delete", e -> {
            deviceKeyHandler.delete(devices);
            dialog.close();
            new SuccessNotification(devices.size() + " devices deleted successfully").open();
            Page<Device> devicePageInitial = deviceService.getPage(
                    PageRequest.of(0, 20, Sort.by("lastJoin")),
                    DeviceSpecification.isOwnedBy(VaadinSession.getCurrent().getAttribute(User.class))
            );
            deviceGrid.setItems(devicePageInitial.stream().toList());
            paginationControls.setMaxPages(devicePageInitial.getTotalPages());
            paginationControls.setPageCounter(1);
            searchField.clear();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton,
                saveButton);
        buttonLayout
                .setJustifyContentMode(JustifyContentMode.END);

        verticalLayout.add(buttonLayout);

        dialog.add(verticalLayout);

        return dialog;
    }


    private Dialog credentialDialog(Device device) {
        Dialog dialog = new Dialog();

        VerticalLayout dialogLayout = new VerticalLayout(new H2("Enter credential password for " + device.getDevEUI()));

        PasswordField passwordField = new PasswordField("Password");
        Button confirmButton = new Button("Confirm", click -> {
            if (passwordField.isEmpty()) {
                Notification.show("Password must not be empty");
            }
            else {
                Optional<KeyCredential> keyCredential = devKeyIdService.findCredentialByDevEUI(device.getDevEUI());
                if (keyCredential.isPresent()) {
                    if (deviceKeyHandler.getCredential(keyCredential.get().getIdentifier()).equals(passwordField.getValue())) {
                        dialogLayout.add(new Text("Root Key: " + Hex.encodeHexString(deviceKeyHandler.getAppKey1_0(device.getDevEUI()).getEncoded())));
                    }
                }
            }
        });

        dialogLayout.add(passwordField, confirmButton);

        dialog.add(dialogLayout);

        return dialog;
    }


}
