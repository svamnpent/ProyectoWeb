package com.phantom.api.controller;

import com.phantom.api.security.Roles;
import com.phantom.api.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/backoffice/auditoria")
@PreAuthorize(Roles.HAS_ADMIN)
public class AuditoriaController {
    private final AuditoriaService auditoria;

    @GetMapping
    public AuditoriaService.Informe informe(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
        return auditoria.consultar(desde, hasta);
    }
}
