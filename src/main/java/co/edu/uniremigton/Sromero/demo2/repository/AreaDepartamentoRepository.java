package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.AreaDepartamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AreaDepartamentoRepository extends JpaRepository<AreaDepartamento, Long> {
    List<AreaDepartamento> findByActivoTrueOrderByNombreAsc();
    List<AreaDepartamento> findAllByOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
}
