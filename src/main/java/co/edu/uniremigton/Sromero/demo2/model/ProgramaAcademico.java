package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "programas_academicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramaAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "programa_id")
    private Long programaId;

    @NotBlank(message = "El nombre del programa es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150, unique = true)
    private String nombre;

    @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
