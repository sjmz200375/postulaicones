package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Column(name = "tipo_doc", nullable = false, length = 5)
    private String tipoDoc;

    @NotBlank(message = "El número de documento es obligatorio")
    @Column(name = "nro_doc", nullable = false, length = 20)
    private String nroDoc;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @NotNull(message = "El tipo de postulación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_postulacion", nullable = false)
    private TipoPostulacion tipoPostulacion;

    @Column(name = "comentarios", length = 500, columnDefinition = "TEXT")
    private String comentarios;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPostulacion estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "terc_id")
    private Long tercId;

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
