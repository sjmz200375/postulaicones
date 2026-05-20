package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.Tercero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Paginados
    Page<Tercero> findAllByOrderByTercApellidosAsc(Pageable pageable);
    Page<Tercero> findByTercTipoOrderByTercApellidosAsc(String tercTipo, Pageable pageable);

    @Query("SELECT t FROM Tercero t WHERE " +
           "LOWER(CONCAT(t.tercNombres, ' ', t.tercApellidos)) LIKE LOWER(CONCAT('%', :nombre, '%')) OR " +
           "LOWER(CONCAT(t.tercApellidos, ' ', t.tercNombres)) LIKE LOWER(CONCAT('%', :nombre, '%')) OR " +
           "t.tercNroDoc LIKE CONCAT('%', :nombre, '%')" +
           " ORDER BY t.tercApellidos ASC")
    Page<Tercero> buscarPaginado(@Param("nombre") String nombre, Pageable pageable);
}