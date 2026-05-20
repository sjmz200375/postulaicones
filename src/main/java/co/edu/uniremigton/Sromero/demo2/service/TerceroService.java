package co.edu.uniremigton.Sromero.demo2.service;

import co.edu.uniremigton.Sromero.demo2.model.EstadoPostulacion;
import co.edu.uniremigton.Sromero.demo2.model.Tercero;
import co.edu.uniremigton.Sromero.demo2.repository.PostulacionRepository;
import co.edu.uniremigton.Sromero.demo2.repository.TerceroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TerceroService {

    private final TerceroRepository repo;
    private final PostulacionRepository postulacionRepo;
    private final HistorialService historialService;

    public List<Tercero> listar()                      { return repo.findAll(); }
    public List<Tercero> buscar(String nombre)          { return repo.buscar(nombre); }
    public Optional<Tercero> porId(Long id)            { return repo.findById(id); }

    public List<Tercero> listarPorTipo(String tipo) {
        return repo.findByTercTipoOrderByTercApellidosAsc(tipo);
    }

    public Page<Tercero> listarPaginado(int page, int size) {
        return repo.findAllByOrderByTercApellidosAsc(PageRequest.of(page, size));
    }

    public Page<Tercero> listarPorTipoPaginado(String tipo, int page, int size) {
        return repo.findByTercTipoOrderByTercApellidosAsc(tipo, PageRequest.of(page, size));
    }

    public Page<Tercero> buscarPaginado(String nombre, int page, int size) {
        return repo.buscarPaginado(nombre, PageRequest.of(page, size));
    }

    @Transactional
    public Tercero actualizar(Long id, Tercero datos) {
        Tercero t = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Tercero no encontrado: " + id));
        t.setTercNombres(datos.getTercNombres());
        t.setTercApellidos(datos.getTercApellidos());
        t.setTercTipoDoc(datos.getTercTipoDoc());
        t.setTercNroDoc(datos.getTercNroDoc());
        t.setTercDireccion(datos.getTercDireccion());
        t.setTercTelefono(datos.getTercTelefono());
        t.setTercMovil(datos.getTercMovil());
        t.setTercTipo(datos.getTercTipo());
        t.setTercEmail(datos.getTercEmail());
        t.setDepartamentoId(datos.getDepartamentoId());
        t.setDepartamentoNombre(datos.getDepartamentoNombre());
        t.setMunicipioId(datos.getMunicipioId());
        t.setMunicipioNombre(datos.getMunicipioNombre());
        return repo.save(t);
    }

    @Transactional
    public void eliminar(Long id, Long userId, String username) {
        Tercero t = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Tercero no encontrado: " + id));

        String descripcion = t.getTercNombres() + " " + t.getTercApellidos() +
                             " (" + t.getTercTipoDoc() + " " + t.getTercNroDoc() + ")";

        historialService.registrar("TERCERO", id, descripcion,
            null, null, "TERCERO_ELIMINADO", userId, username);

        postulacionRepo.findByTercId(id).ifPresent(p -> {
            String estadoAnterior = p.getEstado().name();
            p.setEstado(EstadoPostulacion.PENDIENTE);
            p.setTercId(null);
            postulacionRepo.save(p);

            String descPost = p.getNombres() + " " + p.getApellidos() +
                              " (" + p.getTipoDoc() + " " + p.getNroDoc() + ")";
            historialService.registrar("POSTULACION", p.getId(), descPost,
                estadoAnterior, "PENDIENTE", "POSTULACION_RESETEADA", userId, username);
        });

        repo.deleteById(id);
    }
}
