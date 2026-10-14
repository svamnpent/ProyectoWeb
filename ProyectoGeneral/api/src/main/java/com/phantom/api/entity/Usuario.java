package com.phantom.api.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String rol = "USER";

    @Column(unique = true, length = 20)
    private String dni;

    @Column(length = 255)
    private String direccion;

    @Column(name = "numero_telefono", length = 20)
    private String numeroTelefono;

    // timestamptz en BD. Lo asigna Hibernate al insertar (antes quedaba en null en la respuesta
    // porque la columna era insertable=false y nunca se refrescaba tras el INSERT).
    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro;

    // Relación 1 a 1 bidireccional con Perfil
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Perfil perfil;
}