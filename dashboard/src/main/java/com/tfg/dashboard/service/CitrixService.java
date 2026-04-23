package com.tfg.dashboard.service;

import org.springframework.stereotype.Service;
import com.tfg.dashboard.model.CitrixUsers;

import java.util.Random;

@Service
public class CitrixService {

    public CitrixUsers getConnectedUsers() {
        Random random = new Random();
        int users = 50 + random.nextInt(100); // simula entre 50 y 150 usuarios
        return new CitrixUsers(users);
    }
}