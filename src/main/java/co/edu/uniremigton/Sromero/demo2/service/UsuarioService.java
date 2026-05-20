package co.edu.uniremigton.Sromero.demo2.service;

import co.edu.uniremigton.Sromero.demo2.model.Usuario;
import co.edu.uniremigton.Sromero.demo2.repository.TerceroRepository;
import co.edu.uniremigton.Sromero.demo2.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repo;
    private final TerceroRepository terceroRepo;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listar() {
        return repo.findAll();
    }

    public Page<Usuario> listarPaginado(int page, int size) {
        return repo.findAllByOrderByUserIdAsc(PageRequest.of(page, size));
    }

    @Transactional
    public Usuario crear(Usuario u) {
        u.setUserId(null);
        if (u.getUserActivo() == null) u.setUserActivo(true);
        if (u.getUserRol() == null || u.getUserRol().isBlank()) u.setUserRol("ADMIN");
        if (u.getUserPassword() != null && !u.getUserPassword().isBlank()) {
            u.setUserPassword(passwordEncoder.encode(u.getUserPassword()));
        }
        return repo.save(u);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario u = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        u.setUserUsername(datos.getUserUsername());
        if (datos.getUserPassword() != null && !datos.getUserPassword().isBlank()) {
            u.setUserPassword(passwordEncoder.encode(datos.getUserPassword()));
        }
        if (datos.getUserRol() != null && !datos.getUserRol().isBlank())
            u.setUserRol(datos.getUserRol());
        if (datos.getUserActivo() != null)
            u.setUserActivo(datos.getUserActivo());
        if (datos.getTercId() != null)
            u.setTercId(datos.getTercId());
        return repo.save(u);
    }

    public void eliminar(Long id) { repo.deleteById(id); }

    public Map<String, Object> buscarPorUsername(String username) {
        return repo.findByUserUsername(username)
            .map(u -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("userId",   u.getUserId());
                m.put("username", u.getUserUsername());
                m.put("rol",      u.getUserRol());
                m.put("tercId",   u.getTercId() != null ? u.getTercId() : "");
                return m;
            })
            .orElse(Map.of("error", true, "mensaje", "Usuario no encontrado"));
    }

    public Map<String, Object> login(String username, String password) {
        return repo.findByUserUsername(username)
            .filter(u -> Boolean.TRUE.equals(u.getUserActivo()))
            .filter(u -> passwordEncoder.matches(password, u.getUserPassword()))
            .map(u -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("userId",   u.getUserId());
                m.put("username", u.getUserUsername());
                m.put("rol",      u.getUserRol());
                m.put("tercId",   u.getTercId() != null ? u.getTercId() : "");
                return m;
            })
            .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
    }
}
