package com.github.jonasmelchior.js.ui.views.device;

import com.github.jonasmelchior.js.data.device.AppSKeyReqLog;
import com.github.jonasmelchior.js.data.device.spec.AppSKeyReqLogSpecification;
import com.github.jonasmelchior.js.data.device.spec.JoinLogSpecification;
import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.KeyType;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.ui.views.components.*;
import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.JoinLog;
import com.github.jonasmelchior.js.data.device.SessionState;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.binary.Hex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DeviceView extends VerticalLayout {
    Device device;
    DeviceKeyHandler deviceKeyHandler;
    DeviceService deviceService;
    JoinLogService joinLogService;
    AppSKeyReqLogService appSKeyReqLogService;
    Tab[] tabOptions = {
            new Tab("Session Status"),
            new Tab("Configuration"),
            new Tab("Join Log"),
            new Tab("AppSKeyReq Log")
    };
    Tabs tabs;
    VerticalLayout sessionLayout;
    VerticalLayout configurationLayout;
    VerticalLayout joinLogLayout;
    VerticalLayout appSKeyReqLogLayout;

    VerticalLayout mainLayout;

    public DeviceView(Device device,
                      DeviceService deviceService,
                      DeviceKeyHandler deviceKeyHandler,
                      JoinLogService joinLogService,
                      AppSKeyReqLogService appSKeyReqLogService
    ) {
        this.device = device;
        this.deviceService = deviceService;
        this.deviceKeyHandler = deviceKeyHandler;
        this.joinLogService = joinLogService;
        this.appSKeyReqLogService  = appSKeyReqLogService;

        setSessionLayout();
        setConfigurationLayout();
        setJoinLogLayout();
        setAppSKeyReqLogLayout();

        tabs = new Tabs(tabOptions);
        tabs.addSelectedChangeListener( change -> {
            setLayout(change.getSelectedTab());
        });

        mainLayout = new VerticalLayout(
                sessionLayout
        );

        add(tabs, mainLayout);
        setSizeFull();
    }



    private void setLayout(Tab selectedTab) {
        if (selectedTab.equals(tabOptions[0])) {
            mainLayout.removeAll();
            mainLayout.add(sessionLayout);
        }
        if (selectedTab.equals(tabOptions[1])) {
            mainLayout.removeAll();
            mainLayout.add(configurationLayout);
        }
        if (selectedTab.equals(tabOptions[2])) {
            mainLayout.removeAll();
            mainLayout.add(joinLogLayout);
        }
        if (selectedTab.equals(tabOptions[3])) {
            mainLayout.removeAll();
            mainLayout.add(appSKeyReqLogLayout);
        }

    }

    private void setSessionLayout() {
        PasswordField appSKeyField = new PasswordField("AppSKey");
        PasswordField nwkSKeyField = new PasswordField("NwkSKey");
        PasswordField fNwkSIntKeyField = new PasswordField("FNwkSIntKey");
        PasswordField sNwkSIntKeyField = new PasswordField("SNwkSIntKey");
        PasswordField nwkSEncKeyField = new PasswordField("NwkSEncKey");
        appSKeyField.setWidth("400px");
        nwkSKeyField.setWidth("400px");
        fNwkSIntKeyField.setWidth("400px");
        sNwkSIntKeyField.setWidth("400px");
        nwkSEncKeyField.setWidth("400px");
        appSKeyField.setReadOnly(true);
        nwkSKeyField.setReadOnly(true);
        fNwkSIntKeyField.setReadOnly(true);
        sNwkSIntKeyField.setReadOnly(true);
        nwkSEncKeyField.setReadOnly(true);

        UI ui = UI.getCurrent();

        VerticalLayout sessionKeysLayout = new VerticalLayout(new H4("Session Keys"));


        if (device.getSessionStatus().getState().equals(SessionState.ACTIVE)) {
            if (device.getMacVersion().equals(MACVersion.LORAWAN_1_0)) {
                Key appSKey = deviceKeyHandler.getAppSKey(device.getDevEUI());
                Key nwkSKey = deviceKeyHandler.getNwkSKey(device.getDevEUI());
                if (appSKey != null) {
                    appSKeyField.setValue(Hex.encodeHexString(appSKey.getEncoded()).toUpperCase());
                }
                else {
                    appSKeyField.setValue("Error");
                }
                if (nwkSKey != null) {
                    nwkSKeyField.setValue(Hex.encodeHexString(nwkSKey.getEncoded()).toUpperCase());
                }
                else {
                    nwkSKeyField.setValue("Error");
                }
                sessionKeysLayout.add(appSKeyField, nwkSKeyField);
            }
            else if (device.getMacVersion().equals(MACVersion.LORAWAN_1_1)) {
                Key appSKey = deviceKeyHandler.getAppSKey(device.getDevEUI());
                Key fNwkSIntKey = deviceKeyHandler.getFNwkSIntKey(device.getDevEUI());
                Key sNwkSIntKey = deviceKeyHandler.getSNwkSIntKey(device.getDevEUI());
                Key nwkSEncKey = deviceKeyHandler.getNwkSEncKey(device.getDevEUI());

                if (appSKey != null) {
                    appSKeyField.setValue(Hex.encodeHexString(appSKey.getEncoded()).toUpperCase());
                }
                else {
                    appSKeyField.setValue("Error");
                }
                if (fNwkSIntKey != null) {
                    fNwkSIntKeyField.setValue(Hex.encodeHexString(fNwkSIntKey.getEncoded()).toUpperCase());
                }
                else {
                    fNwkSIntKeyField.setValue("Error");
                }
                if (sNwkSIntKey != null) {
                    sNwkSIntKeyField.setValue(Hex.encodeHexString(sNwkSIntKey.getEncoded()).toUpperCase());
                }
                else {
                    sNwkSIntKeyField.setValue("Error");
                }
                if (nwkSEncKey != null) {
                    nwkSEncKeyField.setValue(Hex.encodeHexString(nwkSEncKey.getEncoded()).toUpperCase());
                }
                else {
                    nwkSEncKeyField.setValue("Error");
                }
                sessionKeysLayout.add(appSKeyField, fNwkSIntKeyField, sNwkSIntKeyField, nwkSEncKeyField);
            }
        }

        TextField devAddressField = new TextField("Device Address");
        TextField sessionNumField = new TextField("Session Number");
        TextField lastDevNonceUsedField = new TextField("Last DevNonce");
        TextField lastJoinNonceUsedField = new TextField("Last JoinNonce");
        devAddressField.setReadOnly(true);
        sessionNumField.setReadOnly(true);
        lastDevNonceUsedField.setReadOnly(true);
        lastJoinNonceUsedField.setReadOnly(true);


        VerticalLayout devNonceHistoryLayout = new VerticalLayout();
        VerticalLayout joinNonceHistoryLayout = new VerticalLayout();

        for (String devNonce : device.getSessionStatus().getUsedDevNonces()) {
            devNonceHistoryLayout.add(new Span(devNonce));
        }
        for (String joinNonce : device.getSessionStatus().getUsedJoinNonces()) {
            joinNonceHistoryLayout.add(new Span(joinNonce));
        }

        Details devNonceHistoryDetails = new Details("DevNonce History", devNonceHistoryLayout);
        Details joinNonceHistoryDetails = new Details("JoinNonce History", joinNonceHistoryLayout);

        VerticalLayout nonceHistoryLayout = new VerticalLayout(
                devNonceHistoryDetails,
                joinNonceHistoryDetails
        );

        if (device.getSessionStatus().getState().equals(SessionState.ACTIVE)) {
            devAddressField.setValue(device.getSessionStatus().getDevAddr());
            sessionNumField.setValue(device.getSessionStatus().getSessionNum().toString());
            lastDevNonceUsedField.setValue(device.getSessionStatus().getLastDevNonce());
            lastJoinNonceUsedField.setValue(device.getSessionStatus().getLastJoinNonce());
        }
        else {
            devAddressField.setValue("N/A");
            sessionNumField.setValue("N/A");
            lastDevNonceUsedField.setValue("N/A");
            lastJoinNonceUsedField.setValue("N/A");
        }

        VerticalLayout detailsLayout = new VerticalLayout(
                new H4("Details"),
                devAddressField,
                sessionNumField,
                lastDevNonceUsedField,
                lastJoinNonceUsedField
        );

        HorizontalLayout detailsLayoutWrapper = new HorizontalLayout(
                detailsLayout,
                nonceHistoryLayout
        );

        detailsLayoutWrapper.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        sessionLayout = new VerticalLayout(
                sessionKeysLayout,
                detailsLayoutWrapper
        );
    }


    private void setConfigurationLayout() {
        RadioButtonGroup<MACVersion> macVersionButtonGroup = new MACVersionComboBox();
        macVersionButtonGroup.setValue(device.getMacVersion());
        macVersionButtonGroup.setReadOnly(true);

        TextField devEUIField = new TextField("DevEUI");
        PasswordField appKeyField = new PasswordField("AppKey");
        PasswordField nwkKeyField = new PasswordField("NwkKey");
        devEUIField.setWidth("400px");
        appKeyField.setWidth("400px");
        nwkKeyField.setWidth("400px");

        devEUIField.setReadOnly(true);
        appKeyField.setReadOnly(true);
        nwkKeyField.setReadOnly(true);

        devEUIField.setValue(device.getDevEUI());

        VerticalLayout rootKeysLayout = new VerticalLayout(new H4("Root Key(s)"));
        rootKeysLayout.setPadding(false);

        if (device.getMacVersion().equals(MACVersion.LORAWAN_1_0)) {
            Key appKey = deviceKeyHandler.getAppKey1_0(device.getDevEUI());
            if (appKey != null) {
                appKeyField.setValue(Hex.encodeHexString(appKey.getEncoded()).toUpperCase());
            }
            else {
                appKeyField.setValue("Error");
            }
            rootKeysLayout.add(appKeyField);
        }
        else if (device.getMacVersion().equals(MACVersion.LORAWAN_1_1)) {
            Key appKey = deviceKeyHandler.getAppKey1_1(device.getDevEUI());
            Key nwkKey = deviceKeyHandler.getNwkKey1_1(device.getDevEUI());
            if (appKey != null) {
                appKeyField.setValue(Hex.encodeHexString(appKey.getEncoded()).toUpperCase());
            }
            else {
                appKeyField.setValue("Error");
            }
            if (nwkKey != null) {
                nwkKeyField.setValue(Hex.encodeHexString(nwkKey.getEncoded()).toUpperCase());
            }
            else {
                nwkKeyField.setValue("Error");
            }
            rootKeysLayout.add(appKeyField, nwkKeyField);
        }

        macVersionButtonGroup.addValueChangeListener( change -> {
            if (change.getValue().equals(MACVersion.LORAWAN_1_0)) {
                rootKeysLayout.remove(nwkKeyField);
            }
            else if (change.getValue().equals(MACVersion.LORAWAN_1_1)) {
                rootKeysLayout.add(nwkKeyField);
            }
        });

        EditControls editControls = new EditControls();

        editControls.addEditListener( edit -> {
            macVersionButtonGroup.setReadOnly(false);
            devEUIField.setReadOnly(false);
            appKeyField.setReadOnly(false);
            nwkKeyField.setReadOnly(false);
        });

        editControls.addCancelListener( cancel -> {
            macVersionButtonGroup.setReadOnly(true);
            devEUIField.setReadOnly(true);
            appKeyField.setReadOnly(true);
            nwkKeyField.setReadOnly(true);
        });

        //TODO : implement
        editControls.addSaveListener( save -> {
            String oldDevEUI = device.getDevEUI();
            MACVersion fromMACVersion = device.getMacVersion();
            device.setDevEUI(devEUIField.getValue());
            device.setMacVersion(macVersionButtonGroup.getValue());
            Pair<Boolean, String> result = null;
            if (macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_0) ||
                    macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_0_1) ||
                    macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_0_2) ||
                    macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_0_3) ||
                    macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_0_4)) {
                result = deviceKeyHandler.update(
                        oldDevEUI,
                        device,
                        new KeySpec(
                                device.getDevEUI(),
                                appKeyField.getValue().toUpperCase(),
                                KeyType.AppKey1_0
                        ),
                        fromMACVersion,
                        device.getMacVersion()
                );
            }
            else if (macVersionButtonGroup.getValue().equals(MACVersion.LORAWAN_1_1)) {
                result = deviceKeyHandler.update(
                        oldDevEUI,
                        device,
                        new ArrayList<>(List.of(
                                new KeySpec(
                                        device.getDevEUI(),
                                        appKeyField.getValue().toUpperCase(),
                                        KeyType.AppKey1_1
                                ),
                                new KeySpec(
                                        device.getDevEUI(),
                                        nwkKeyField.getValue().toUpperCase(),
                                        KeyType.NwkKey1_1
                                )
                        )),
                        device.getMacVersion(),
                        macVersionButtonGroup.getValue()
                );
            }

            if (result.a) {
                new SuccessNotification("Device " + device.getDevEUI() + " successfully updated").open();

                macVersionButtonGroup.setReadOnly(true);
                devEUIField.setReadOnly(true);
                appKeyField.setReadOnly(true);
                nwkKeyField.setReadOnly(true);
            }
            else {
                new ErrorNotification(result.b).open();
            }


        });

        Button deleteDeviceButton = new Button("Delete Device", new Icon(VaadinIcon.TRASH));
        deleteDeviceButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteDeviceButton.addClickListener( click -> confirmDeleteDialog(device).open());

        configurationLayout = new VerticalLayout(
                macVersionButtonGroup,
                devEUIField,
                rootKeysLayout,
                editControls,
                deleteDeviceButton
        );
    }

    private Dialog confirmDeleteDialog(Device device) {
        Dialog dialog = new Dialog();

        VerticalLayout verticalLayout = new VerticalLayout(
                new H2("Delete Device"),
                new H4("Are you sure you want to delete device " + device.getDevEUI() + "?")
        );

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        Button saveButton = new Button("Delete", e -> {
            deviceKeyHandler.delete(device);
            dialog.close();
            new SuccessNotification("Device " + device.getDevEUI() + " deleted successfully").open();
            UI.getCurrent().navigate(DevicesView.class);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton,
                saveButton);
        buttonLayout
                .setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        verticalLayout.add(buttonLayout);

        dialog.add(verticalLayout);

        return dialog;
    }

    private void setJoinLogLayout() {
        Grid<JoinLog> joinLogGrid = new Grid<>();

        joinLogGrid.addColumn(JoinLog::getJoinReqAttemptTime).setHeader("JoinReq received at");
        joinLogGrid.addComponentColumn( joinLog -> new Button(new Icon(VaadinIcon.INFO), click ->
                createLogJsonDialog(joinLog.getJoinReq()).open()))
                .setHeader("JoinReq");
        joinLogGrid.addComponentColumn( joinLog -> new Button(new Icon(VaadinIcon.INFO), click ->
                        createLogJsonDialog(joinLog.getJoinAns()).open()))
                .setHeader("JoinAns");
        joinLogGrid.addComponentColumn( joinLog -> {
            Icon icon;

            if (joinLog.getSuccess()) {
                icon = new Icon(VaadinIcon.CHECK);
                icon.setColor("green");
            }
            else {
                icon = new Icon(VaadinIcon.MINUS);
                icon.setColor("red");
            }

            return icon;
        }).setHeader("Success");

        Pagination paginationControls = new Pagination();
        Page<JoinLog> joinLogPageInitial = joinLogService.getPage(
                PageRequest.of(0, 20, Sort.by("joinReqAttemptTime")),
                JoinLogSpecification.byDevice(device)
        );
        joinLogGrid.setItems(joinLogPageInitial.getContent());
        paginationControls.setMaxPages(joinLogPageInitial.getTotalPages());
        paginationControls.addPageChangeListener( change -> {
            Page<JoinLog> joinLogPage = joinLogService.getPage(
                    PageRequest.of(change.getNewPageCounterValue() - 1, 20, Sort.by("joinReqAttemptTime")),
                    JoinLogSpecification.byDevice(device)
            );
            joinLogGrid.setItems(joinLogPage.getContent());
        });

        joinLogLayout = new VerticalLayout(
                joinLogGrid,
                paginationControls
        );

        joinLogLayout.setSizeFull();
    }

    private void setAppSKeyReqLogLayout() {
        Grid<AppSKeyReqLog> appSKeyReqLogGrid = new Grid<>();

        appSKeyReqLogGrid.addColumn(AppSKeyReqLog::getAppSKeyReqAttemptTime).setHeader("AppSKeyReq received at");
        appSKeyReqLogGrid.addComponentColumn( appSKeyReqLog -> new Button(new Icon(VaadinIcon.INFO), click ->
                        createLogJsonDialog(appSKeyReqLog.getAppSKeyReq()).open()))
                .setHeader("AppSKeyReq");
        appSKeyReqLogGrid.addComponentColumn( appSKeyReqLog -> new Button(new Icon(VaadinIcon.INFO), click ->
                        createLogJsonDialog(appSKeyReqLog.getAppSKeyAns()).open()))
                .setHeader("AppSKeyAns");
        appSKeyReqLogGrid.addComponentColumn( appSKeyReqLog -> {
            Icon icon;

            if (appSKeyReqLog.getSuccess()) {
                icon = new Icon(VaadinIcon.CHECK);
                icon.setColor("green");
            }
            else {
                icon = new Icon(VaadinIcon.MINUS);
                icon.setColor("red");
            }

            return icon;
        }).setHeader("Success");

        Pagination paginationControls = new Pagination();
        Page<AppSKeyReqLog> appSKeyReqLogPageInitial = appSKeyReqLogService.getPage(
                PageRequest.of(0, 20, Sort.by("appSKeyReqAttemptTime")),
                AppSKeyReqLogSpecification.byDevice(device)
        );

        appSKeyReqLogGrid.setItems(appSKeyReqLogPageInitial.getContent());
        paginationControls.setMaxPages(appSKeyReqLogPageInitial.getTotalPages());
        paginationControls.addPageChangeListener( change -> {
            Page<AppSKeyReqLog> appSKeyReqLogPage = appSKeyReqLogService.getPage(
                    PageRequest.of(change.getNewPageCounterValue() - 1, 20, Sort.by("appSKeyReqAttemptTime")),
                    AppSKeyReqLogSpecification.byDevice(device)
            );
            appSKeyReqLogGrid.setItems(appSKeyReqLogPage.getContent());
        });

        appSKeyReqLogLayout = new VerticalLayout(
                appSKeyReqLogGrid,
                paginationControls
        );

        appSKeyReqLogLayout.setSizeFull();
    }

    private Dialog createLogJsonDialog(Object joinContents) {
        Dialog dialog = new Dialog();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create();

        AceEditor aceEditor = new AceEditor();
        aceEditor.setReadOnly(true);
        aceEditor.setMode(AceMode.json);
        aceEditor.setValue(formatJson(gson.toJson(joinContents)).replaceAll("\\\\", ""));

        aceEditor.setHeight("500px");
        aceEditor.setWidth("700px");

        dialog.add(aceEditor);

        return dialog;
    }

    private String formatJson(String jsonString) {
        // You can use a library like Jackson or Gson for better formatting
        // Here, we are using a simple approach for illustration purposes
        return jsonString.replace(",", ",\n").replace("{", "{\n").replace("}", "\n}");
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
