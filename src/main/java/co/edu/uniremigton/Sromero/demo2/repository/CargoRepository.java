package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    List<Cargo> findByActivoTrueOrderByNombreAsc();
    List<Cargo> findAllByOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
}
