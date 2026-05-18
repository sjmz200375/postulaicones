package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
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

    @Column(name = "user_username", length = 50, nullable = false, unique = true)
    private String userUsername;

    @Column(name = "user_password", length = 255, nullable = false)
    private String userPassword;

    @Column(name = "user_rol", length = 20, nullable = false)
    private String userRol;

    @Column(name = "user_activo")
    private Boolean userActivo = true;

    @Column(name = "terc_id")
    private Long tercId;
}
