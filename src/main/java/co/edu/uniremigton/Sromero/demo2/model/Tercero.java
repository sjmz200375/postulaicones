package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "terceros")
public class Tercero {

    @Id
    @SequenceGenerator(name = "sq_terceros", sequenceName = "sq_terceros", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_terceros")
    @Column(name = "terc_id")
    private Long tercId;

    @Pattern(regexp = "^(CC|TI|CE|PA)$",
             message = "El tipo de documento debe ser CC, TI, CE o PA")
    @Column(name = "terc_tipo_doc", length = 5)
    private String tercTipoDoc;

    @Pattern(regexp = "^[0-9]+$",
             message = "El número de documento solo puede contener números")
    @Size(max = 20, message = "El número de documento no puede superar 20 caracteres")
    @Column(name = "terc_nro_doc", length = 20)
    private String tercNroDoc;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$",
             message = "Los nombres solo pueden contener letras y espacios")
    @Column(name = "terc_nombres", length = 100)
    private String tercNombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$",
             message = "Los apellidos solo pueden contener letras y espacios")
    @Column(name = "terc_apellidos", length = 100)
    private String tercApellidos;

    @Size(max = 100, message = "La dirección no puede superar 100 caracteres")
    @Column(name = "terc_direccion", length = 100)
    private String tercDireccion;

    @Pattern(regexp = "^[0-9]{0,15}$",
             message = "El teléfono solo puede contener números y máximo 15 dígitos")
    @Column(name = "terc_telefono", length = 20)
    private String tercTelefono;

    @Pattern(regexp = "^[0-9]{0,15}$",
             message = "El móvil solo puede contener números y máximo 15 dígitos")
    @Column(name = "terc_movil", length = 20)
    private String tercMovil;

    @Column(name = "terc_tipo", length = 1)
    private String tercTipo;

    @Email(message = "El formato del email no es válido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    @Column(name = "terc_email", length = 150)
    private String tercEmail;

    @Column(name = "departamento_id")
    private Long departamentoId;

    @Column(name = "departamento_nombre", length = 100)
    private String departamentoNombre;

    @Column(name = "municipio_id")
    private Long municipioId;

    @Column(name = "municipio_nombre", length = 100)
    private String municipioNombre;
}
