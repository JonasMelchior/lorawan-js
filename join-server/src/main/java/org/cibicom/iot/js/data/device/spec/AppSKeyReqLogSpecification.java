package org.cibicom.iot.js.data.device.spec;

import jakarta.persistence.criteria.Predicate;
import org.cibicom.iot.js.data.device.AppSKeyReqLog;
import org.cibicom.iot.js.data.device.Device;
import org.springframework.data.jpa.domain.Specification;

public class AppSKeyReqLogSpecification {
    public static Specification<AppSKeyReqLog> byDevice(Device device, Boolean success) {
        return (root, query, builder) -> {
            if (success == null) {
                return builder.equal(root.get("device"), device);
            }
            else {
                Predicate devicePredicate = builder.equal(root.get("device"), device);
                Predicate successPredicate = builder.equal(root.get("success"), success);
                return builder.and(devicePredicate, successPredicate);
            }
        };
    }
}
