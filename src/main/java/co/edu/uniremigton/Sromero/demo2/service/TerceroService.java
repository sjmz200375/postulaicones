package co.edu.uniremigton.Sromero.demo2.service;

import co.edu.uniremigton.Sromero.demo2.model.EstadoPostulacion;
import co.edu.uniremigton.Sromero.demo2.model.Tercero;
import co.edu.uniremigton.Sromero.demo2.repository.PostulacionRepository;
import co.edu.uniremigton.Sromero.demo2.repository.TerceroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TerceroService {

    private final TerceroRepository repo;
    private final PostulacionRepository postulacionRepo;

    public List<Tercero> listar()                      { return repo.findAll(); }
    public List<Tercero> buscar(String nombre)          { return repo.buscar(nombre); }
    public Optional<Tercero> porId(Long id)            { return repo.findById(id); }

    public List<Tercero> listarPorTipo(String tipo) {
        return repo.findByTercTipoOrderByTercApellidosAsc(tipo);
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
        return repo.save(t);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Tercero no encontrado: " + id);
        }
        postulacionRepo.findByTercId(id).ifPresent(p -> {
            p.setEstado(EstadoPostulacion.PENDIENTE);
            p.setTercId(null);
            postulacionRepo.save(p);
        });
        repo.deleteById(id);
    }
}
