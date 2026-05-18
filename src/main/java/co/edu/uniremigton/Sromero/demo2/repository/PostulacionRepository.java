package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.EstadoPostulacion;
import co.edu.uniremigton.Sromero.demo2.model.Postulacion;
import co.edu.uniremigton.Sromero.demo2.model.TipoPostulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {

    List<Postulacion> findByEstadoOrderByFechaCreacionDesc(EstadoPostulacion estado);

    List<Postulacion> findByTipoPostulacionOrderByFechaCreacionDesc(TipoPostulacion tipo);

    List<Postulacion> findByEstadoAndTipoPostulacionOrderByFechaCreacionDesc(EstadoPostulacion estado, TipoPostulacion tipo);

    Optional<Postulacion> findByNroDocAndTipoPostulacion(String nroDoc, TipoPostulacion tipo);

    Optional<Postulacion> findByTercId(Long tercId);

    @Query("SELECT p FROM Postulacion p WHERE " +
           "LOWER(CONCAT(p.nombres, ' ', p.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(CONCAT(p.apellidos, ' ', p.nombres)) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "p.nroDoc LIKE CONCAT('%', :texto, '%')")
    List<Postulacion> buscar(@Param("texto") String texto);

    @Query("SELECT COUNT(p) FROM Postulacion p WHERE p.estado = :estado")
    long countByEstado(@Param("estado") EstadoPostulacion estado);

    @Query("SELECT COUNT(p) FROM Postulacion p WHERE p.tipoPostulacion = :tipo")
    long countByTipo(@Param("tipo") TipoPostulacion tipo);
}
