package com.cicam.backend.service;

import com.cicam.backend.dto.Dtos.PerfilRequest;
import com.cicam.backend.dto.Dtos.PerfilResponse;
import com.cicam.backend.model.Perfil;
import com.cicam.backend.model.Rol;
import com.cicam.backend.model.Usuario;
import com.cicam.backend.repository.GrupoIntegranteRepository;
import com.cicam.backend.repository.PerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Perfiles de usuario.
 * Ver: el propio usuario o cualquier persona que comparta un grupo con él.
 * Editar: cada familiar y cada cuidador edita el suyo; el perfil del adulto mayor lo editan
 * los familiares de su grupo y el propio adulto mayor.
 */
@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfiles;
    private final GrupoIntegranteRepository integrantes;
    private final AccesoService acceso;

    @Transactional(readOnly = true)
    public PerfilResponse obtener(Long solicitanteId, Long usuarioId) {
        Usuario objetivo = acceso.usuario(usuarioId);
        if (!puedeVer(solicitanteId, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este perfil");
        }
        Perfil p = perfiles.findById(usuarioId).orElse(null);
        return respuesta(objetivo, p, puedeEditar(solicitanteId, objetivo));
    }

    @Transactional
    public PerfilResponse guardar(Long solicitanteId, Long usuarioId, PerfilRequest r) {
        Usuario objetivo = acceso.usuario(usuarioId);
        if (!puedeEditar(solicitanteId, objetivo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar este perfil");
        }
        validarFoto(r.foto());

        if (r.nombre() != null && !r.nombre().isBlank()) objetivo.setNombre(r.nombre().trim());
        if (objetivo.getRol() == Rol.ADULTO_MAYOR && r.fechaNacimiento() != null) {
            if (!r.fechaNacimiento().isBefore(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento debe ser pasada");
            }
            objetivo.setFechaNacimiento(r.fechaNacimiento());
        }

        Perfil p = perfiles.findById(usuarioId).orElseGet(() -> {
            Perfil nuevo = new Perfil();
            nuevo.setUsuarioId(usuarioId);
            return nuevo;
        });
        p.setFoto(vacio(r.foto()));
        p.setTelefono(vacio(r.telefono()));
        p.setDireccion(vacio(r.direccion()));
        p.setAcercaDe(vacio(r.acercaDe()));
        // Solo se guardan los datos que corresponden al rol del usuario.
        boolean familiar = objetivo.getRol() == Rol.FAMILIAR;
        boolean cuidador = objetivo.getRol() == Rol.CUIDADOR;
        boolean adulto = objetivo.getRol() == Rol.ADULTO_MAYOR;
        p.setParentesco(familiar ? vacio(r.parentesco()) : null);
        p.setExperiencia(cuidador ? vacio(r.experiencia()) : null);
        p.setDisponibilidad(cuidador ? vacio(r.disponibilidad()) : null);
        p.setGrupoSanguineo(adulto ? vacio(r.grupoSanguineo()) : null);
        p.setAlergias(adulto ? vacio(r.alergias()) : null);
        p.setCondicionesMedicas(adulto ? vacio(r.condicionesMedicas()) : null);
        p.setEstadoSalud(adulto ? vacio(r.estadoSalud()) : null);
        p.setSeguroMedico(adulto ? vacio(r.seguroMedico()) : null);
        p.setContactoEmergenciaNombre(adulto ? vacio(r.contactoEmergenciaNombre()) : null);
        p.setContactoEmergenciaTelefono(adulto ? vacio(r.contactoEmergenciaTelefono()) : null);
        p.setActualizado(LocalDateTime.now());
        perfiles.save(p);
        return respuesta(objetivo, p, true);
    }

    // ---------- reglas ----------

    private boolean puedeVer(Long solicitanteId, Long usuarioId) {
        return solicitanteId.equals(usuarioId) || integrantes.gruposCompartidos(solicitanteId, usuarioId) > 0;
    }

    private boolean puedeEditar(Long solicitanteId, Usuario objetivo) {
        if (solicitanteId.equals(objetivo.getId())) return true;
        if (objetivo.getRol() != Rol.ADULTO_MAYOR) return false; // el perfil de un familiar/cuidador es solo suyo
        Usuario solicitante = acceso.usuario(solicitanteId);
        return solicitante.getRol() == Rol.FAMILIAR
                && integrantes.gruposCompartidos(solicitanteId, objetivo.getId()) > 0;
    }

    private void validarFoto(String foto) {
        if (foto != null && !foto.isBlank()
                && (foto.length() > 1400000 || !foto.matches("data:image/(jpeg|png|webp);base64,[A-Za-z0-9+/=]+"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fotografía debe ser JPG, PNG o WebP y menor a 1 MB");
        }
    }

    private static String vacio(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private PerfilResponse respuesta(Usuario u, Perfil p, boolean puedeEditar) {
        Integer edad = u.getFechaNacimiento() == null ? null
                : Period.between(u.getFechaNacimiento(), LocalDate.now()).getYears();
        if (p == null) p = new Perfil();
        return new PerfilResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getFechaNacimiento(), edad,
                puedeEditar, p.getFoto(), p.getTelefono(), p.getDireccion(), p.getAcercaDe(),
                p.getParentesco(), p.getExperiencia(), p.getDisponibilidad(),
                p.getGrupoSanguineo(), p.getAlergias(), p.getCondicionesMedicas(), p.getEstadoSalud(),
                p.getSeguroMedico(), p.getContactoEmergenciaNombre(), p.getContactoEmergenciaTelefono());
    }
}
