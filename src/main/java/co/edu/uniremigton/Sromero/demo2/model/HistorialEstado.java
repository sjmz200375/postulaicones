package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "historial_id")
    private Long historialId;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 20)
    private String estadoNuevo;

    @Column(name = "accion", length = 50)
    private String accion;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @PrePersist
    public void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
    }
}
