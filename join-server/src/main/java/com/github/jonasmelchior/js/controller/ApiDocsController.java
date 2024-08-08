package com.github.jonasmelchior.js.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping("/docs")
public class ApiDocsController {
    @GetMapping
    public String index() {
        return "api-guide.html";  // Assuming 'page.html' is the name of your static HTML file
    }
}