package com.tfg.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.dashboard.dto.summary.Microsoft365Summary;
import com.tfg.dashboard.service.Microsoft365Service;

/**
 * Endpoint REST de Microsoft 365.
 *
 * La fuente es simulada en backend y persistida en MySQL. La generación queda
 * reservada al proceso de sincronización; la pantalla consulta siempre el
 * último snapshot disponible.
 */
@RestController
@RequestMapping("/microsoft365")
public class Microsoft365Controller {

        private final Microsoft365Service microsoft365Service;

        public Microsoft365Controller(Microsoft365Service microsoft365Service) {
                this.microsoft365Service = microsoft365Service;
        }

        // Devuelve uso, seguridad, dispositivos, SharePoint y el índice de salud Microsoft 365 normalizado.
         
        @GetMapping("/summary")
        public Microsoft365Summary getSummary() {
                return microsoft365Service.getSummary();
        }
}
