package co.edu.uniremigton.Sromero.demo2.service;

import co.edu.uniremigton.Sromero.demo2.model.*;
import co.edu.uniremigton.Sromero.demo2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ProgramaAcademicoRepository programaRepo;
    private final AreaDepartamentoRepository areaRepo;
    private final CargoRepository cargoRepo;

    public List<ProgramaAcademico> listarProgramas(boolean soloActivos) {
        return soloActivos
            ? programaRepo.findByActivoTrueOrderByNombreAsc()
            : programaRepo.findAllByOrderByNombreAsc();
    }

    @Transactional
    public ProgramaAcademico crearPrograma(ProgramaAcademico p) {
        if (programaRepo.existsByNombreIgnoreCase(p.getNombre())) {
            throw new RuntimeException("Ya existe un programa con ese nombre");
        }
        p.setProgramaId(null);
        if (p.getActivo() == null) p.setActivo(true);
        return programaRepo.save(p);
    }

    @Transactional
    public ProgramaAcademico actualizarPrograma(Long id, ProgramaAcademico datos) {
        ProgramaAcademico p = programaRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Programa no encontrado: " + id));
        p.setNombre(datos.getNombre());
        p.setDescripcion(datos.getDescripcion());
        if (datos.getActivo() != null) p.setActivo(datos.getActivo());
        return programaRepo.save(p);
    }

    public void eliminarPrograma(Long id) {
        if (!programaRepo.existsById(id)) {
            throw new RuntimeException("Programa no encontrado: " + id);
        }
        programaRepo.deleteById(id);
    }

    public List<AreaDepartamento> listarAreas(boolean soloActivos) {
        return soloActivos
            ? areaRepo.findByActivoTrueOrderByNombreAsc()
            : areaRepo.findAllByOrderByNombreAsc();
    }

    @Transactional
    public AreaDepartamento crearArea(AreaDepartamento a) {
        if (areaRepo.existsByNombreIgnoreCase(a.getNombre())) {
            throw new RuntimeException("Ya existe un área con ese nombre");
        }
        a.setAreaId(null);
        if (a.getActivo() == null) a.setActivo(true);
        return areaRepo.save(a);
    }

    @Transactional
    public AreaDepartamento actualizarArea(Long id, AreaDepartamento datos) {
        AreaDepartamento a = areaRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Área no encontrada: " + id));
        a.setNombre(datos.getNombre());
        a.setDescripcion(datos.getDescripcion());
        if (datos.getActivo() != null) a.setActivo(datos.getActivo());
        return areaRepo.save(a);
    }

    public void eliminarArea(Long id) {
        if (!areaRepo.existsById(id)) {
            throw new RuntimeException("Área no encontrada: " + id);
        }
        areaRepo.deleteById(id);
    }

    public List<Cargo> listarCargos(boolean soloActivos) {
        return soloActivos
            ? cargoRepo.findByActivoTrueOrderByNombreAsc()
            : cargoRepo.findAllByOrderByNombreAsc();
    }

    @Transactional
    public Cargo crearCargo(Cargo c) {
        if (cargoRepo.existsByNombreIgnoreCase(c.getNombre())) {
            throw new RuntimeException("Ya existe un cargo con ese nombre");
        }
        c.setCargoId(null);
        if (c.getActivo() == null) c.setActivo(true);
        return cargoRepo.save(c);
    }

    @Transactional
    public Cargo actualizarCargo(Long id, Cargo datos) {
        Cargo c = cargoRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Cargo no encontrado: " + id));
        c.setNombre(datos.getNombre());
        c.setDescripcion(datos.getDescripcion());
        if (datos.getActivo() != null) c.setActivo(datos.getActivo());
        return cargoRepo.save(c);
    }

    public void eliminarCargo(Long id) {
        if (!cargoRepo.existsById(id)) {
            throw new RuntimeException("Cargo no encontrado: " + id);
        }
        cargoRepo.deleteById(id);
    }
}
