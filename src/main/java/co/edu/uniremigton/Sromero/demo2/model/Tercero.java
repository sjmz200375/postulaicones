package co.edu.uniremigton.Sromero.demo2.model;

import jakarta.persistence.*;
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

    @Column(name = "terc_tipo_doc", length = 2)
    private String tercTipoDoc;

    @Column(name = "terc_nro_doc", length = 10)
    private String tercNroDoc;

    @Column(name = "terc_nombres", length = 50)
    private String tercNombres;

    @Column(name = "terc_apellidos", length = 50)
    private String tercApellidos;

    @Column(name = "terc_direccion", length = 50)
    private String tercDireccion;

    @Column(name = "terc_telefono", length = 10)
    private String tercTelefono;

    @Column(name = "terc_movil", length = 10)
    private String tercMovil;

    @Column(name = "terc_tipo", length = 1)
    private String tercTipo;

    @Column(name = "terc_email", length = 150)
    private String tercEmail;
}