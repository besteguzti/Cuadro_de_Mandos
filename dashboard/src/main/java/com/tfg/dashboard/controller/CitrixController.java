package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tfg.dashboard.service.CitrixService;
import com.tfg.dashboard.model.CitrixUsers;

@RestController
public class CitrixController {

    private final CitrixService citrixService;

    public CitrixController(CitrixService citrixService) {
        this.citrixService = citrixService;
    }

    @GetMapping("/citrix/users")
    public CitrixUsers getUsers() {
        return citrixService.getConnectedUsers();
    }
}
