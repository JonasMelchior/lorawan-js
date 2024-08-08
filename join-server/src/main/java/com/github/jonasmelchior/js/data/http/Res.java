package com.github.jonasmelchior.js.data.http;

import com.fasterxml.jackson.annotation.JsonView;
import com.github.jonasmelchior.js.json.Views;
import org.springframework.http.HttpStatus;

public class Res {
    @JsonView(Views.Public.class)
    HttpStatus httpStatus;
    @JsonView(Views.Public.class)
    String message;

    public Res(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
