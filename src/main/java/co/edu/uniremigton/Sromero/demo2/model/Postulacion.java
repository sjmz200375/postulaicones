package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "postulaciones", uniqueConstraints = {
    @UniqueConstraint(name = "uk_postulacion_doc_tipo", columnNames = {"nro_doc", "tipo_postulacion"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$",
             message = "Los nombres solo pueden contener letras y espacios")
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$",
             message = "Los apellidos solo pueden contener letras y espacios")
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "^(CC|TI|CE|PA)$",
             message = "El tipo de documento debe ser CC, TI, CE o PA")
    @Column(name = "tipo_doc", nullable = false, length = 5)
    private String tipoDoc;

    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "^[0-9]+$",
             message = "El número de documento solo puede contener números")
    @Size(max = 20, message = "El número de documento no puede superar 20 caracteres")
    @Column(name = "nro_doc", nullable = false, length = 20)
    private String nroDoc;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Pattern(regexp = "^[0-9]{0,15}$",
             message = "El teléfono solo puede contener números y máximo 15 dígitos")
    @Column(name = "telefono", length = 20)
    private String telefono;

    @NotNull(message = "El tipo de postulación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_postulacion", nullable = false, length = 20)
    private TipoPostulacion tipoPostulacion;

    @Size(max = 500, message = "Los comentarios no pueden superar 500 caracteres")
    @Column(name = "comentarios", length = 500)
    private String comentarios;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPostulacion estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "terc_id")
    private Long tercId;

    @Column(name = "programa_academico_id")
    private Long programaAcademicoId;

    @Column(name = "area_departamento_id")
    private Long areaDepartamentoId;

    @Column(name = "cargo_id")
    private Long cargoId;

    @PrePersist
    private void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoPostulacion.PENDIENTE;
        }
    }
}
