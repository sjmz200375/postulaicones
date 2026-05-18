package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.Tercero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TerceroRepository extends JpaRepository<Tercero, Long> {

    @Query("SELECT t FROM Tercero t WHERE " +
           "LOWER(CONCAT(t.tercNombres, ' ', t.tercApellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')) OR " +
           "LOWER(CONCAT(t.tercApellidos, ' ', t.tercNombres)) LIKE LOWER(CONCAT('%', :nombre, '%')) OR " +
           "t.tercNroDoc LIKE CONCAT('%', :nombre, '%')")
    List<Tercero> buscar(@Param("nombre") String nombre);

    List<Tercero> findByTercTipoOrderByTercApellidosAsc(String tercTipo);
    List<Tercero> findByTercTipo(String tercTipo);
}