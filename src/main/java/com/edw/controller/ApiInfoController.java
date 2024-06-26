package com.edw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiInfoController {

    @GetMapping("/apiInfo")
    public String apiInfo() {
        return "apiInfo";
    }

    @GetMapping("/session")
    public String session() {
        return "session";
    }

    @GetMapping("/log")
    public String log() {
        return "log";
    }

}
