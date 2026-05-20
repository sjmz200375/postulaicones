package co.edu.uniremigton.Sromero.demo2.service;

import co.edu.uniremigton.Sromero.demo2.model.Departamento;
import co.edu.uniremigton.Sromero.demo2.model.Municipio;
import co.edu.uniremigton.Sromero.demo2.repository.DepartamentoRepository;
import co.edu.uniremigton.Sromero.demo2.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final DepartamentoRepository departamentoRepo;
    private final MunicipioRepository municipioRepo;

    public List<Departamento> listarDepartamentos() {
        return departamentoRepo.findAllByOrderByNombreAsc();
    }

    public List<Municipio> listarMunicipiosPorDepartamento(Long departamentoId) {
        return municipioRepo.findByDepartamento_DepartamentoIdOrderByNombreAsc(departamentoId);
    }

    public List<Municipio> listarTodosMunicipios() {
        return municipioRepo.findAllByOrderByNombreAsc();
    }
}
