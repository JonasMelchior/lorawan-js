package com.github.jonasmelchior.js.service.device.keys;

import org.springframework.security.authentication.AuthenticationServiceException;

public class CredentialAuthenticationException extends Exception {

    public CredentialAuthenticationException(String message) {
        super(message);
    }

}
