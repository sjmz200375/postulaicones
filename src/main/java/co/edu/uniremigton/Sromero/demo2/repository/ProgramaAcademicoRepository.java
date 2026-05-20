package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.ProgramaAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProgramaAcademicoRepository extends JpaRepository<ProgramaAcademico, Long> {
    List<ProgramaAcademico> findByActivoTrueOrderByNombreAsc();
    List<ProgramaAcademico> findAllByOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
}
