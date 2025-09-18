package org.cibicom.iot.js.ui.views.device;

import com.vaadin.flow.component.checkbox.Checkbox;
import org.cibicom.iot.js.service.utils.lrwan.LrWanUtils;
import org.cibicom.iot.js.ui.views.components.ErrorNotification;
import org.cibicom.iot.js.ui.views.components.MACVersionComboBox;
import org.cibicom.iot.js.ui.views.components.SuccessNotification;
import org.cibicom.iot.js.data.keys.KeyCredential;
import org.cibicom.iot.js.data.keys.KeySpec;
import org.cibicom.iot.js.data.keys.KeyType;
import org.cibicom.iot.js.data.lrwan.MACVersion;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.device.keys.DeviceKeyHandler;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import org.antlr.v4.runtime.misc.Pair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class RegisterDevicesView extends VerticalLayout {
    Tabs tabs;

    private final Tab[] tabOptions = {
            new Tab(new Text("Single")),
            new Tab(new Text("In Bulk"))
    };

    DeviceKeyHandler deviceKeyHandler;
    KeyCredentialService keyCredentialService;
    DeviceService deviceService;
    VerticalLayout registerSingleLayout;
    VerticalLayout registerBulkLayout;
    String allowedEuiAndKeyPattern = "[0-9A-Fa-f]";

    public RegisterDevicesView(DeviceKeyHandler deviceKeyHandler,
                               KeyCredentialService keyCredentialService,
                               DeviceService deviceService
    ) {
        this.deviceKeyHandler = deviceKeyHandler;
        this.keyCredentialService = keyCredentialService;
        this.deviceService = deviceService;

        setRegisterSingleLayout();
        setRegisterInBulkLayout();

        tabs = new Tabs(tabOptions);
        tabs.addSelectedChangeListener( change -> {
            setLayout(change.getSelectedTab());
        });

        add(tabs, registerSingleLayout);
    }

    private void setLayout(Tab tab) {
        if (tab.equals(tabOptions[0])) {
            remove(registerBulkLayout);
            add(registerSingleLayout);
        }
        else if (tab.equals(tabOptions[1])) {
            remove(registerSingleLayout);
            add(registerBulkLayout);
        }
    }


    public void setRegisterSingleLayout() {
        TextField devEUIField =  new TextField("DevEUI");
        devEUIField.setWidth("400px");
        devEUIField.setRequired(true);
        devEUIField.setAllowedCharPattern(allowedEuiAndKeyPattern);
        devEUIField.setMaxLength(16);
        devEUIField.setMinLength(16);

        RadioButtonGroup<MACVersion> macVersionButtonGroup = new MACVersionComboBox();
        macVersionButtonGroup.setValue(MACVersion.LORAWAN_1_0);

        VerticalLayout keysLayout = new VerticalLayout();

        TextField appKeyField = new TextField("AppKey");
        TextField nwkKeyField = new TextField("NwkKey");
        appKeyField.setWidth("400px");
        nwkKeyField.setWidth("400px");
        appKeyField.setRequired(true);
        nwkKeyField.setRequired(true);
        appKeyField.setAllowedCharPattern(allowedEuiAndKeyPattern);
        appKeyField.setMaxLength(32);
        appKeyField.setMinLength(32);
        nwkKeyField.setAllowedCharPattern(allowedEuiAndKeyPattern);
        nwkKeyField.setMaxLength(32);
        nwkKeyField.setMinLength(32);

        keysLayout.add(macVersionButtonGroup, appKeyField);

        macVersionButtonGroup.addValueChangeListener( change -> {

            if (LrWanUtils.getMacVersions1_0().contains(change.getValue())) {
                keysLayout.remove(nwkKeyField);
            }
            else if (change.getValue().equals(MACVersion.LORAWAN_1_1)) {
                keysLayout.add(nwkKeyField);
            }
        });

        RadioButtonGroup<String> kekEnabledButtonGroup = new RadioButtonGroup<>("KEK");
        kekEnabledButtonGroup.setItems("Wrap keys when under transportation", "No wrapping");
        kekEnabledButtonGroup.setValue("Wrap keys when under transportation");

        TextField kekLabelField = new TextField("KEK Label");
        TextField wrappingKeyField = new TextField("Wrapping Key");
        kekLabelField.setWidth("400px");
        wrappingKeyField.setWidth("400px");
        kekLabelField.setRequired(true);
        wrappingKeyField.setRequired(true);

        VerticalLayout kekLayout = new VerticalLayout(kekEnabledButtonGroup, kekLabelField, wrappingKeyField);
        kekLayout.setPadding(false);
        kekEnabledButtonGroup.addValueChangeListener( change -> {
            if (change.getValue().equals("Wrap keys when under transportation")) {
                kekLayout.add(kekLabelField, wrappingKeyField);
            }
            else if (change.getValue().equals("No wrapping")) {
                kekLayout.remove(kekLabelField, wrappingKeyField);
            }
        });

        Checkbox sendAppSKeyToNSCheckbox = new Checkbox("Send AppSKey to Network Server in JoinAns");
        sendAppSKeyToNSCheckbox.setValue(true);

        RadioButtonGroup<String> credentialTypeButtonGroup = new RadioButtonGroup<>("Secure with credential");
        credentialTypeButtonGroup.setItems("New Credential", "Existing Credential");
        credentialTypeButtonGroup.setValue("New Credential");

        VerticalLayout credentialLayout = new VerticalLayout();

        TextField newCredentialIdField = new TextField("Credential Identifier");
        TextField newCredentialPasswordField = new TextField("Password");
        newCredentialIdField.setWidth("400px");
        newCredentialPasswordField.setWidth("400px");
        newCredentialIdField.setRequired(true);
        newCredentialPasswordField.setRequired(true);

        ComboBox<KeyCredential> existingCredential = new ComboBox<>("Registered Credentials");
        existingCredential.setWidth("400px");
        existingCredential.setItems(keyCredentialService.findByOwner(VaadinSession.getCurrent().getAttribute(User.class)));

        credentialLayout.add(credentialTypeButtonGroup, newCredentialIdField, newCredentialPasswordField);

        credentialTypeButtonGroup.addValueChangeListener( change -> {
            if (change.getValue().equals("New Credential")) {
                credentialLayout.remove(existingCredential);
                credentialLayout.add(newCredentialIdField);
                credentialLayout.add(newCredentialPasswordField);
            }
            else if (change.getValue().equals("Existing Credential")) {
                credentialLayout.remove(newCredentialIdField);
                credentialLayout.remove(newCredentialPasswordField);
                credentialLayout.add(existingCredential);
            }
        });

        Button registerButton = new Button("Register", new Icon(VaadinIcon.PLUS));
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener( click -> {
            if (devEUIField.getValue().length() != 16
                    || appKeyField.getValue().length() != 32
                    || (macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_1) &&
                    nwkKeyField.getValue().length() != 32)) {
                new ErrorNotification("Invalid DevEUI or Key(s)").open();
            }
            else if (credentialTypeButtonGroup.getValue().equals("New Credential") &&
                    (newCredentialIdField.isEmpty() || newCredentialPasswordField.isEmpty())) {
                new ErrorNotification("Credential must be filled out when selecting new").open();
            }
            else if (credentialTypeButtonGroup.getValue().equals("Existing Credential") &&
                    existingCredential.isEmpty()) {
                new ErrorNotification("Existing credential must be selected").open();
            }
            else if (deviceService.findByDevEUI(devEUIField.getValue().toUpperCase()).isPresent()) {
                new ErrorNotification("Provided DevEUI already exists on the join server").open();
            }
            else {
                Pair<Boolean, String> result = null;
                KeySpec kek = null;
                if (kekEnabledButtonGroup.getValue().equals("Wrap keys when under transportation")) {
                    kek = new KeySpec(
                            kekLabelField.getValue(),
                            wrappingKeyField.getValue(),
                            KeyType.KEK
                    );
                }

                if (LrWanUtils.getMacVersions1_0().contains(macVersionButtonGroup.getValue())) {
                    if (credentialTypeButtonGroup.getValue().equals("Existing Credential")) {
                        result = deviceKeyHandler.init(
                                new KeySpec(devEUIField.getValue().toUpperCase(), appKeyField.getValue().toUpperCase(), KeyType.AppKey1_0),
                                kek,
                                existingCredential.getValue(),
                                VaadinSession.getCurrent().getAttribute(User.class),
                                false,
                                macVersionButtonGroup.getValue(),
                                sendAppSKeyToNSCheckbox.getValue()
                        );
                    }
                    else {
                        result = deviceKeyHandler.init(
                                new KeySpec(devEUIField.getValue().toUpperCase(), appKeyField.getValue().toUpperCase(), KeyType.AppKey1_0),
                                kek,
                                newCredentialPasswordField.getValue(),
                                newCredentialIdField.getValue(),
                                VaadinSession.getCurrent().getAttribute(User.class),
                                false,
                                macVersionButtonGroup.getValue(),
                                sendAppSKeyToNSCheckbox.getValue()
                        );
                    }
                }
                else if (macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_1)) {
                    if (credentialTypeButtonGroup.getValue().equals("Existing Credential")) {
                        result = deviceKeyHandler.init(
                                new ArrayList<>(List.of(
                                        new KeySpec(devEUIField.getValue().toUpperCase(), appKeyField.getValue().toUpperCase(), KeyType.AppKey1_1),
                                        new KeySpec(devEUIField.getValue().toUpperCase(), nwkKeyField.getValue().toUpperCase(), KeyType.NwkKey1_1)
                                )),
                                kek,
                                existingCredential.getValue(),
                                VaadinSession.getCurrent().getAttribute(User.class),
                                false,
                                macVersionButtonGroup.getValue(),
                                sendAppSKeyToNSCheckbox.getValue()
                        );
                    }
                    else {
                        result = deviceKeyHandler.init(
                                new ArrayList<>(List.of(
                                        new KeySpec(devEUIField.getValue().toUpperCase(), appKeyField.getValue().toUpperCase(), KeyType.AppKey1_1),
                                        new KeySpec(devEUIField.getValue().toUpperCase(), nwkKeyField.getValue().toUpperCase(), KeyType.NwkKey1_1)
                                )),
                                kek,
                                newCredentialPasswordField.getValue(),
                                newCredentialIdField.getValue(),
                                VaadinSession.getCurrent().getAttribute(User.class),
                                false,
                                macVersionButtonGroup.getValue(),
                                sendAppSKeyToNSCheckbox.getValue()
                        );
                    }
                }

                if (result == null) {
                    new ErrorNotification("An internal error occured").open();
                }
                else if (!result.a) {
                    new ErrorNotification(result.b).open();
                }
                else {
                    UI.getCurrent().navigate(DevicesView.class);
                }

            }
        });

        keysLayout.setPadding(false);
        credentialLayout.setPadding(false);

        registerSingleLayout = new VerticalLayout(
                devEUIField,
                keysLayout,
                kekLayout,
                sendAppSKeyToNSCheckbox,
                credentialLayout,
                registerButton
        );
    }

    private void setRegisterInBulkLayout() {
        RadioButtonGroup<MACVersion> macVersionButtonGroup = new MACVersionComboBox();
        macVersionButtonGroup.setValue(MACVersion.LORAWAN_1_0);

        RadioButtonGroup<String> credentialTypeButtonGroup = new RadioButtonGroup<>("Secure with credential");
        credentialTypeButtonGroup.setItems("New Credential", "Existing Credential");
        credentialTypeButtonGroup.setValue("New Credential");

        VerticalLayout credentialLayout = new VerticalLayout();

        TextField newCredentialIdField = new TextField("Credential Identifier");
        TextField newCredentialPasswordField = new TextField("Password");
        newCredentialIdField.setWidth("400px");
        newCredentialPasswordField.setWidth("400px");
        newCredentialIdField.setRequired(true);
        newCredentialPasswordField.setRequired(true);

        ComboBox<KeyCredential> existingCredential = new ComboBox<>("Registered Credentials");
        existingCredential.setWidth("400px");
        existingCredential.setItems(keyCredentialService.findByOwner(VaadinSession.getCurrent().getAttribute(User.class)));

        credentialLayout.add(credentialTypeButtonGroup, newCredentialIdField, newCredentialPasswordField);

        credentialTypeButtonGroup.addValueChangeListener( change -> {
            if (change.getValue().equals("New Credential")) {
                credentialLayout.remove(existingCredential);
                credentialLayout.add(newCredentialIdField);
                credentialLayout.add(newCredentialPasswordField);
            }
            else if (change.getValue().equals("Existing Credential")) {
                credentialLayout.remove(newCredentialIdField);
                credentialLayout.remove(newCredentialPasswordField);
                credentialLayout.add(existingCredential);
            }
        });

        RadioButtonGroup<String> kekEnabledButtonGroup = new RadioButtonGroup<>("KEK");
        kekEnabledButtonGroup.setItems("Wrap keys when under transportation", "No wrapping");
        kekEnabledButtonGroup.setValue("Wrap keys when under transportation");

        TextField kekLabelField = new TextField("KEK Label");
        TextField wrappingKeyField = new TextField("Wrapping Key");
        kekLabelField.setWidth("400px");
        wrappingKeyField.setWidth("400px");
        kekLabelField.setRequired(true);
        wrappingKeyField.setRequired(true);

        Checkbox sendAppSKeyToNSCheckbox = new Checkbox("Send AppSKey to Network Server in JoinAns");
        sendAppSKeyToNSCheckbox.setValue(true);

        VerticalLayout kekLayout = new VerticalLayout(kekEnabledButtonGroup, kekLabelField, wrappingKeyField);
        kekLayout.setPadding(false);
        kekEnabledButtonGroup.addValueChangeListener( change -> {
            if (change.getValue().equals("Wrap keys when under transportation")) {
                kekLayout.add(kekLabelField, wrappingKeyField);
            }
            else if (change.getValue().equals("No wrapping")) {
                kekLayout.remove(kekLabelField, wrappingKeyField);
            }
        });

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv");
        upload.setMaxFileSize(100 * 1024 * 1024); // Max file size

        List<KeySpec> rootKeySpecs = new ArrayList<>();

        AtomicBoolean isUploadValid = new AtomicBoolean(false);
        macVersionButtonGroup.addValueChangeListener( change -> {
           isUploadValid.set(false);
        });

        upload.addSucceededListener( success -> {
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader reader =
                    new CSVReaderBuilder(new InputStreamReader(buffer.getInputStream())).withCSVParser(parser).build();

            try {
                List<String[]> entries = reader.readAll();
                String[] headers = entries.get(0);
                isUploadValid.set(true);

                if (LrWanUtils.getMacVersions1_0().contains(macVersionButtonGroup.getValue())) {
                    if (headers.length == 2 &&
                            headers[0] != null && headers[0].equalsIgnoreCase("deveui") &&
                            headers[1] != null && headers[1].equalsIgnoreCase("appkey")) {
                        for (String[] row : entries.subList(1, entries.size())) {
                            if (row.length == 2 &&
                                    row[0] != null && isValidHexString(row[0], 16) &&
                                    row[1] != null && isValidHexString(row[1], 32)) {
                                rootKeySpecs.add(new KeySpec(
                                        row[0].toUpperCase(),
                                        row[1].toUpperCase(),
                                        KeyType.AppKey1_0
                                ));
                            }
                            else {
                                isUploadValid.set(false);
                                break;
                            }
                        }
                        if (isUploadValid.get()) {
                            new SuccessNotification("CSV successfully parsed").open();
                        }
                        else {
                            new ErrorNotification("CSV data fields has wrong format at some place. Please check the format, and try again").open();
                        }
                    }
                    else {
                        new ErrorNotification("Wrong header in CSV. Make sure to include 'DevEUI' and 'AppKey' in the header. Please check the format, and try again").open();
                    }
                }
                else if (macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_1)) {
                    if (headers.length == 3 &&
                            headers[0] != null && headers[0].equalsIgnoreCase("deveui") &&
                            headers[1] != null && headers[1].equalsIgnoreCase("appkey") &&
                            headers[2] != null && headers[2].equalsIgnoreCase("nwkkey") ) {
                        for (String[] row : entries.subList(1, entries.size())) {
                            if (row.length == 3 &&
                                    row[0] != null && isValidHexString(row[0], 16) &&
                                    row[1] != null && isValidHexString(row[1], 32) &&
                                    row[2] != null && isValidHexString(row[2], 32)) {
                                rootKeySpecs.add(new KeySpec(
                                        row[0].toUpperCase(),
                                        row[1].toUpperCase(),
                                        KeyType.AppKey1_1
                                ));
                                rootKeySpecs.add(new KeySpec(
                                        row[0].toUpperCase(),
                                        row[2].toUpperCase(),
                                        KeyType.NwkKey1_1
                                ));
                            }
                            else {
                                isUploadValid.set(false);
                                break;
                            }
                        }
                        if (isUploadValid.get()) {
                            new SuccessNotification("CSV successfully parsed").open();
                        }
                        else {
                            new ErrorNotification("CSV data fields has wrong format at some place. Please check the format, and try again").open();
                        }
                    }
                    else {
                        new ErrorNotification("Wrong header in CSV. Make sure to include 'DevEUI' and 'AppKey' in the header. Please check the format, and try again").open();
                    }
                }

            } catch (IOException | CsvException e) {
                new ErrorNotification("An error occurred parsing the CSV file. Please check the format, and try again").open();
            }
        });

        ProgressBar progressBar = new ProgressBar();
        progressBar.setWidth("15em");
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        VerticalLayout progressLayout = new VerticalLayout(
                progressBar
        );

        VerticalLayout progressLayoutDescription = new VerticalLayout(
                new Span("Securely storing keys..."),
                new Span("You can close this window or leave it open since the task will finish in the background")
        );

        progressLayoutDescription.setPadding(false);

        Span successSpan = new Span("Keys stored successfully");
        successSpan.getStyle().set("color", "green");

        progressLayout.add(progressLayoutDescription, successSpan);

        progressLayoutDescription.setVisible(false);
        successSpan.setVisible(false);

        progressLayout.setPadding(false);


        Button registerButton = new Button("Register", new Icon(VaadinIcon.PLUS));
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener( click -> {
            if (credentialTypeButtonGroup.getValue().equals("New Credential") &&
                    (newCredentialIdField.isEmpty() || newCredentialPasswordField.isEmpty())) {
                new ErrorNotification("Credential must be filled out when selecting new").open();
            }
            else if (credentialTypeButtonGroup.getValue().equals("Existing Credential") &&
                    existingCredential.isEmpty()) {
                new ErrorNotification("Existing credential must be selected").open();
            }
            else if (!isUploadValid.get()) {
                new ErrorNotification("Valid CSV has not been uploaded").open();
            }
            else if (deviceService.isDuplicate(rootKeySpecs.stream().map(KeySpec::getIdentifier).toList())) {
                new ErrorNotification("One or more DevEUIs parsed in the CSV already exist on the join server").open();
            }
            else {
                KeySpec kek;
                if (kekEnabledButtonGroup.getValue().equals("Wrap keys when under transportation")) {
                    kek = new KeySpec(
                            kekLabelField.getValue(),
                            wrappingKeyField.getValue(),
                            KeyType.KEK
                    );
                } else {
                    kek = null;
                }
                if (LrWanUtils.getMacVersions1_0().contains(macVersionButtonGroup.getValue())) {
                    if (credentialTypeButtonGroup.getValue().equals("Existing Credential")) {
                        UI ui = click.getSource().getUI().orElseThrow();
                        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);

                        progressBar.setVisible(true);
                        progressLayoutDescription.setVisible(true);
                        successSpan.setVisible(false);

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.submit(() -> {
                            deviceKeyHandler.init(
                                    rootKeySpecs,
                                    kek,
                                    existingCredential.getValue(),
                                    currentUser,
                                    false,
                                    macVersionButtonGroup.getValue(),
                                    sendAppSKeyToNSCheckbox.getValue()
                            );

                            ui.access(() -> {
                                progressBar.setVisible(false);
                                progressLayoutDescription.setVisible(false);
                                successSpan.setVisible(true);
                            });
                        });
                        executor.shutdown();
                    }
                    else {
                        UI ui = click.getSource().getUI().orElseThrow();
                        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);

                        progressBar.setVisible(true);
                        progressLayoutDescription.setVisible(true);
                        successSpan.setVisible(false);

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.submit(() -> {
                            deviceKeyHandler.init(
                                    rootKeySpecs,
                                    kek,
                                    newCredentialPasswordField.getValue(),
                                    newCredentialIdField.getValue(),
                                    currentUser,
                                    false,
                                    macVersionButtonGroup.getValue(),
                                    sendAppSKeyToNSCheckbox.getValue()
                            );

                            ui.access(() -> {
                                progressBar.setVisible(false);
                                progressLayoutDescription.setVisible(false);
                                successSpan.setVisible(true);
                            });
                        });

                        // Shutdown the executor after submitting the task
                        executor.shutdown();
                    }
                }
                else if (macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_1)) {
                    if (credentialTypeButtonGroup.getValue().equals("Existing Credential")) {
                        UI ui = click.getSource().getUI().orElseThrow();
                        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);

                        progressBar.setVisible(true);
                        progressLayoutDescription.setVisible(true);
                        successSpan.setVisible(false);

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.submit(() -> {
                            deviceKeyHandler.init(
                                    rootKeySpecs,
                                    kek,
                                    existingCredential.getValue(),
                                    currentUser,
                                    false,
                                    MACVersion.LORAWAN_1_1,
                                    sendAppSKeyToNSCheckbox.getValue()
                            );

                            ui.access(() -> {
                                progressBar.setVisible(false);
                                progressLayoutDescription.setVisible(false);
                                successSpan.setVisible(true);
                            });
                        });

                        executor.shutdown();
                    }
                    else {
                        UI ui = click.getSource().getUI().orElseThrow();
                        User currentUser = VaadinSession.getCurrent().getAttribute(User.class);

                        progressBar.setVisible(true);
                        progressLayoutDescription.setVisible(true);
                        successSpan.setVisible(false);

                        ExecutorService executor = Executors.newSingleThreadExecutor();

                        executor.submit(() -> {
                            deviceKeyHandler.init(
                                    rootKeySpecs,
                                    kek,
                                    newCredentialPasswordField.getValue(),
                                    newCredentialIdField.getValue(),
                                    currentUser,
                                    false,
                                    MACVersion.LORAWAN_1_1,
                                    sendAppSKeyToNSCheckbox.getValue()
                            );

                            ui.access(() -> {
                                progressBar.setVisible(false);
                                progressLayoutDescription.setVisible(false);
                                successSpan.setVisible(true);
                            });
                        });
                        executor.shutdown();
                    }
                }

            }
        });

        credentialLayout.setPadding(false);
        upload.setWidth("400px");

        VerticalLayout registerLayout = new VerticalLayout(
                progressLayout,
                macVersionButtonGroup,
                kekLayout,
                sendAppSKeyToNSCheckbox,
                credentialLayout,
                upload,
                registerButton
        );

        HorizontalLayout bulkLayout = new HorizontalLayout(
                registerLayout,
                createBulkFormatGuideLayout()
        );



        registerBulkLayout = new VerticalLayout(
                bulkLayout
        );
    }

    private VerticalLayout createBulkFormatGuideLayout() {
        VerticalLayout formatGuideLayout = new VerticalLayout();

        Tab[] tabOptionsMac = {
                new Tab("1.0"),
                new Tab("1.1")
        };

        Tabs tabsMacOption = new Tabs(tabOptionsMac);

        String mac1_0FormatExample = "DevEUI;AppKey\n" +
                "8FD2A1B7E94C3F6E;3B7F29E8D6A501C2F9D5E8276C3A40BE\n" +
                "5AEDB8F372C64D91;E6C8DAF9B247310FC5A620D83BDE0A85";

        String mac1_1FormatExample = "DevEUI;AppKey;NwkKey\n" +
                "8FD2A1B7E94C3F6E;3B7F29E8D6A501C2F9D5E8276C3A40BE;71B9E8A5D3F0276C84D10ABE92356FC2\n" +
                "5AEDB8F372C64D91;E6C8DAF9B247310FC5A620D83BDE0A85;4F8D217E3A9BC605D2EFA0487B63F912";


        AceEditor aceEditor = new AceEditor();
        aceEditor.setMode(AceMode.text);
        aceEditor.setValue(mac1_0FormatExample);

        aceEditor.setWidth("650px");

        tabsMacOption.addSelectedChangeListener( change -> {
            if (change.getSelectedTab().equals(tabOptionsMac[0])) {
                aceEditor.setValue(mac1_0FormatExample);
            }
            else if (change.getSelectedTab().equals(tabOptionsMac[1])) {
                aceEditor.setValue(mac1_1FormatExample);
            }
        });

        VerticalLayout verticalLayout = new VerticalLayout(
                new H3("CSV format"),
                tabsMacOption,
                aceEditor
        );
        verticalLayout.setSizeFull();
        return verticalLayout;
    }

    private boolean isValidHexString(String str, int length) {
        // Regular expression to match hexadecimal characters
        String hexPattern = "^[0-9A-Fa-f]+$";

        // Check if the string matches the hexadecimal pattern
        if (!Pattern.matches(hexPattern, str)) {
            return false;
        }

        // Check if the length matches the required length
        if (str.length() != length) {
            return false;
        }

        return true;
    }

}
