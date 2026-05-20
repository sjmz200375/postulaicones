package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.HistorialEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HistorialRepository
        extends JpaRepository<HistorialEstado, Long>,
                JpaSpecificationExecutor<HistorialEstado> {

    Page<HistorialEstado> findAllByOrderByFechaDesc(Pageable pageable);
    Page<HistorialEstado> findByTipoOrderByFechaDesc(String tipo, Pageable pageable);
    Page<HistorialEstado> findByUsernameContainingIgnoreCaseOrderByFechaDesc(String username, Pageable pageable);
    Page<HistorialEstado> findByTipoAndUsernameContainingIgnoreCaseOrderByFechaDesc(String tipo, String username, Pageable pageable);
}
