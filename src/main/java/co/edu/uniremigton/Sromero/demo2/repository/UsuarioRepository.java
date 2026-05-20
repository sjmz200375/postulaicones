package co.edu.uniremigton.Sromero.demo2.repository;

import co.edu.uniremigton.Sromero.demo2.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUserUsername(String username);
    Optional<Usuario> findByTercId(Long tercId);
    Page<Usuario> findAllByOrderByUserIdAsc(Pageable pageable);
}
