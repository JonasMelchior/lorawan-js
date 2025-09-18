package org.cibicom.iot.js.ui.views.err;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import org.cibicom.iot.js.ui.views.components.ErrorNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomErrorHandler implements ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void error(ErrorEvent errorEvent) {
        logger.error("Something wrong happened", errorEvent.getThrowable());
        if(UI.getCurrent() != null) {
            UI.getCurrent().access(() -> {
                new ErrorNotification("An internal error has occurred." +
                        "Contact support for assistance.").open();
            });
        }
    }
}