package co.edu.uniremigton.Sromero.demo2.service;

import co.edu.uniremigton.Sromero.demo2.model.EstadoPostulacion;
import co.edu.uniremigton.Sromero.demo2.model.Postulacion;
import co.edu.uniremigton.Sromero.demo2.model.Tercero;
import co.edu.uniremigton.Sromero.demo2.model.TipoPostulacion;
import co.edu.uniremigton.Sromero.demo2.repository.PostulacionRepository;
import co.edu.uniremigton.Sromero.demo2.repository.TerceroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostulacionService {

    private final PostulacionRepository repo;
    private final TerceroRepository terceroRepo;

    public List<Postulacion> listar() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    }

    public List<Postulacion> listarPorEstado(EstadoPostulacion estado) {
        return repo.findByEstadoOrderByFechaCreacionDesc(estado);
    }

    public List<Postulacion> listarPorTipo(TipoPostulacion tipo) {
        return repo.findByTipoPostulacionOrderByFechaCreacionDesc(tipo);
    }

    public List<Postulacion> listarPorEstadoYTipo(EstadoPostulacion estado, TipoPostulacion tipo) {
        return repo.findByEstadoAndTipoPostulacionOrderByFechaCreacionDesc(estado, tipo);
    }

    public List<Postulacion> buscar(String texto) {
        return repo.buscar(texto);
    }

    public Optional<Postulacion> porId(Long id) {
        return repo.findById(id);
    }

    public Postulacion crear(Postulacion p) {
        p.setId(null);
        p.setTercId(null);
        p.setEstado(EstadoPostulacion.PENDIENTE);

        repo.findByNroDocAndTipoPostulacion(p.getNroDoc(), p.getTipoPostulacion())
            .ifPresent(existing -> {
                throw new RuntimeException("Ya existe una postulación con ese documento para el tipo seleccionado");
            });

        return repo.save(p);
    }

    @Transactional
    public Postulacion aprobar(Long id) {
        Postulacion p = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada: " + id));

        if (p.getEstado() == EstadoPostulacion.APROBADA) {
            throw new RuntimeException("La postulación ya está aprobada");
        }

        Tercero t = new Tercero();
        t.setTercNombres(p.getNombres());
        t.setTercApellidos(p.getApellidos());
        t.setTercTipoDoc(p.getTipoDoc());
        t.setTercNroDoc(p.getNroDoc());
        t.setTercTelefono(p.getTelefono());
        t.setTercEmail(p.getEmail());
        t.setTercTipo(mapearTipo(p.getTipoPostulacion()));
        t.setTercDireccion(null);
        t.setTercMovil(null);

        Tercero guardado = terceroRepo.save(t);

        p.setTercId(guardado.getTercId());
        p.setEstado(EstadoPostulacion.APROBADA);
        return repo.save(p);
    }

    @Transactional
    public Postulacion rechazar(Long id) {
        Postulacion p = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada: " + id));

        if (p.getEstado() == EstadoPostulacion.APROBADA) {
            throw new RuntimeException("Una postulación aprobada no puede rechazarse aquí. Elimine el tercero asociado en el módulo de Terceros.");
        }

        p.setEstado(EstadoPostulacion.RECHAZADA);
        return repo.save(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Postulacion p = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Postulación no encontrada: " + id));

        if (p.getEstado() == EstadoPostulacion.APROBADA && p.getTercId() != null) {
            throw new RuntimeException("Una postulación aprobada no puede eliminarse aquí. Elimine el tercero asociado en el módulo de Terceros.");
        }

        repo.deleteById(id);
    }

    private String mapearTipo(TipoPostulacion tipo) {
        return switch (tipo) {
            case ESTUDIANTE     -> "0";
            case PROFESOR       -> "1";
            case ADMINISTRATIVO -> "2";
        };
    }
}
