package com.cicam.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Perfil de un usuario (FAMILIAR, CUIDADOR o ADULTO_MAYOR). Una fila por usuario.
 * Los datos de salud solo se usan para el adulto mayor; parentesco, para el familiar;
 * experiencia y disponibilidad, para el cuidador.
 */
@Entity
@Table(name = "perfiles")
@Getter
@Setter
@NoArgsConstructor
public class Perfil {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Fotografía como data URI base64 (JPG/PNG/WebP, menor a 1 MB). */
    @Lob
    @Column(name = "foto", columnDefinition = "LONGTEXT")
    private String foto;

    @Column(length = 30)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @Column(length = 1000)
    private String acercaDe;

    // --- Familiar ---
    @Column(length = 60)
    private String parentesco;

    // --- Cuidador ---
    @Column(length = 1000)
    private String experiencia;

    @Column(length = 300)
    private String disponibilidad;

    // --- Adulto mayor ---
    @Column(length = 10)
    private String grupoSanguineo;

    @Column(length = 1000)
    private String alergias;

    @Column(length = 1000)
    private String condicionesMedicas;

    @Column(length = 1000)
    private String estadoSalud;

    @Column(length = 150)
    private String seguroMedico;

    @Column(length = 120)
    private String contactoEmergenciaNombre;

    @Column(length = 30)
    private String contactoEmergenciaTelefono;

    @Column(nullable = false)
    private LocalDateTime actualizado = LocalDateTime.now();
}
