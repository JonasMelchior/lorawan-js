package com.github.jonasmelchior.js.data.device.spec;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class DeviceSpecification {
    public static Specification<Device> isOwnedBy(User owner) {
        return (root, query, builder) -> builder.equal(root.get("owner"), owner);
    }

    public static Specification<Device> isOwnedByAndContainsEui(User owner, String devEUI) {
        return (root, query, builder) -> {
            Predicate ownerPredicate = builder.equal(root.get("owner"), owner);
            Predicate devEUIPredicate = builder.like(root.get("devEUI"), "%" + devEUI.toUpperCase() + "%");
            return builder.and(ownerPredicate, devEUIPredicate);
        };
    }
}
