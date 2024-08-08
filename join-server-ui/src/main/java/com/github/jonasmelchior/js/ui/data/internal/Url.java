package com.github.jonasmelchior.js.ui.data.internal;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Url {
    private static final HttpServletRequest httpServletRequest = ((VaadinServletRequest) VaadinRequest.getCurrent()).getHttpServletRequest();
    private static final String hostname = httpServletRequest.getServerName();
    private static final String urlPrefix = "http://" +  hostname + ":8080/";

    public static String getUrlPrefix() {
        return urlPrefix;
    }
}
