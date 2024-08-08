package com.github.jonasmelchior.js.ui.views.components;


import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

public class MACVersionComboBox extends RadioButtonGroup<MACVersion> {

    public MACVersionComboBox() {
        setLabel("LoRaWAN Version");
        setItems(MACVersion.values());
        setItemLabelGenerator( macVersion -> {
            switch (macVersion) {
                case LORAWAN_1_0 -> {
                    return "1.0";
                }
                case LORAWAN_1_0_1 -> {
                    return "1.0.1";
                }
                case LORAWAN_1_0_2 -> {
                    return "1.0.2";
                }
                case LORAWAN_1_0_3 -> {
                    return "1.0.3";
                }
                case LORAWAN_1_0_4 -> {
                    return "1.0.4";
                }
                case LORAWAN_1_1 -> {
                    return "1.1";
                }
                default -> {
                    return "N/A";
                }
            }
        });
    }
}
