package com.github.jonasmelchior.js.data.device.spec;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.JoinLog;
import org.springframework.data.jpa.domain.Specification;

public class JoinLogSpecification {
    public static Specification<JoinLog> byDevice(Device device) {
        return (root, query, builder) -> builder.equal(root.get("device"), device);
    }

}
