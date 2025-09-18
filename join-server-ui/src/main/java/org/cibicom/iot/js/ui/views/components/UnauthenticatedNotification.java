package org.cibicom.iot.js.ui.views.components;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class UnauthenticatedNotification extends Notification {

    public UnauthenticatedNotification() {
        setText("Invalid username or password");
        addThemeVariants(NotificationVariant.LUMO_PRIMARY, NotificationVariant.LUMO_ERROR);
        setPosition(Position.TOP_CENTER);
        setDuration(5000);
    }
}
