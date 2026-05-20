package co.edu.uniremigton.Sromero.demo2.service;

import co.edu.uniremigton.Sromero.demo2.model.HistorialEstado;
import co.edu.uniremigton.Sromero.demo2.repository.HistorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistorialService {

    private final HistorialRepository repo;

    @Transactional
    public void registrar(String tipo, Long referenciaId, String descripcion,
                          String estadoAnterior, String estadoNuevo,
                          String accion, Long userId, String username) {
        HistorialEstado h = new HistorialEstado();
        h.setTipo(tipo);
        h.setReferenciaId(referenciaId);
        h.setDescripcion(descripcion);
        h.setEstadoAnterior(estadoAnterior);
        h.setEstadoNuevo(estadoNuevo);
        h.setAccion(accion);
        h.setUserId(userId);
        h.setUsername(username != null ? username : "sistema");
        h.setFecha(LocalDateTime.now());
        repo.save(h);
    }

    public Page<HistorialEstado> filtrar(String tipo, String username,
                                          String desde, String hasta,
                                          int page, int size) {
        Specification<HistorialEstado> spec = (root, q, cb) -> cb.conjunction();

        if (tipo != null && !tipo.isBlank()) {
            String tipoUpper = tipo.toUpperCase();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("tipo"), tipoUpper));
        }
        if (username != null && !username.isBlank()) {
            String u = username.toLowerCase();
            spec = spec.and((root, q, cb) ->
                cb.like(cb.lower(root.get("username")), "%" + u + "%"));
        }
        if (desde != null && !desde.isBlank()) {
            LocalDateTime desdeDt = LocalDate.parse(desde).atStartOfDay();
            spec = spec.and((root, q, cb) ->
                cb.greaterThanOrEqualTo(root.get("fecha"), desdeDt));
        }
        if (hasta != null && !hasta.isBlank()) {
            LocalDateTime hastaDt = LocalDate.parse(hasta).atTime(23, 59, 59);
            spec = spec.and((root, q, cb) ->
                cb.lessThanOrEqualTo(root.get("fecha"), hastaDt));
        }

        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha"));
        return repo.findAll(spec, pr);
    }
}
