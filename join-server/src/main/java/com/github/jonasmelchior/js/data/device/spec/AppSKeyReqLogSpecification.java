package com.github.jonasmelchior.js.data.device.spec;

import com.github.jonasmelchior.js.data.device.AppSKeyReqLog;
import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.JoinLog;
import org.springframework.data.jpa.domain.Specification;

public class AppSKeyReqLogSpecification {
    public static Specification<AppSKeyReqLog> byDevice(Device device) {
        return (root, query, builder) -> builder.equal(root.get("device"), device);
    }
}
