package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_usuarios")
    @SequenceGenerator(name = "sq_usuarios",
                       sequenceName = "sq_usuarios",
                       allocationSize = 1)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "El username solo puede contener letras, números y guion bajo")
    @Column(name = "user_username", length = 50, nullable = false, unique = true)
    private String userUsername;

    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    @Column(name = "user_password", length = 255, nullable = false)
    private String userPassword;

    @Column(name = "user_rol", length = 20, nullable = false)
    private String userRol;

    @Column(name = "user_activo")
    private Boolean userActivo = true;

    @Column(name = "terc_id")
    private Long tercId;
}
