package com.openconsult.openldap_xml_processor.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LDAPAuthenticationController {

    @GetMapping("/")
    public String index() {
        return "Welcome to the home page!";
    }
}
