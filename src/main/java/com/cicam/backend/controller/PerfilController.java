package com.cicam.backend.controller;

import com.cicam.backend.dto.Dtos.PerfilRequest;
import com.cicam.backend.dto.Dtos.PerfilResponse;
import com.cicam.backend.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfiles")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService service;

    @GetMapping("/{usuarioId}")
    public PerfilResponse obtener(@AuthenticationPrincipal Long solicitanteId, @PathVariable Long usuarioId) {
        return service.obtener(solicitanteId, usuarioId);
    }

    @PutMapping("/{usuarioId}")
    public PerfilResponse guardar(@AuthenticationPrincipal Long solicitanteId, @PathVariable Long usuarioId,
                                  @Valid @RequestBody PerfilRequest r) {
        return service.guardar(solicitanteId, usuarioId, r);
    }
}
