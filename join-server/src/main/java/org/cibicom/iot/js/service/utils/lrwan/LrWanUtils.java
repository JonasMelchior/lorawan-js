package org.cibicom.iot.js.service.utils.lrwan;

import org.cibicom.iot.js.data.lrwan.MACVersion;

import java.util.ArrayList;
import java.util.List;

public class LrWanUtils {
    public static List<MACVersion> getMacVersions1_0() {
        return new ArrayList<>(List.of(
                MACVersion.LORAWAN_1_0,
                MACVersion.LORAWAN_1_0_1,
                MACVersion.LORAWAN_1_0_2,
                MACVersion.LORAWAN_1_0_3,
                MACVersion.LORAWAN_1_0_4
        ));
    }
}
