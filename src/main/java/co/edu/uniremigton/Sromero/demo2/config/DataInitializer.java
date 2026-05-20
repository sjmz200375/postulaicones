package co.edu.uniremigton.Sromero.demo2.config;

import co.edu.uniremigton.Sromero.demo2.model.Departamento;
import co.edu.uniremigton.Sromero.demo2.model.Municipio;
import co.edu.uniremigton.Sromero.demo2.repository.DepartamentoRepository;
import co.edu.uniremigton.Sromero.demo2.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DepartamentoRepository departamentoRepo;
    private final MunicipioRepository municipioRepo;

    @Override
    public void run(String... args) {
        if (departamentoRepo.count() > 0) return;

        Departamento cordoba = new Departamento();
        cordoba.setNombre("Córdoba");
        cordoba = departamentoRepo.save(cordoba);

        Departamento antioquia = new Departamento();
        antioquia.setNombre("Antioquia");
        antioquia = departamentoRepo.save(antioquia);

        List<String> municipiosCordoba = Arrays.asList(
            "Montería", "Cereté", "Sahagún", "Lorica", "Montelíbano",
            "Tierralta", "Planeta Rica", "Ayapel", "Chinú", "San Pelayo",
            "Ciénaga de Oro", "Buenavista", "Pueblo Nuevo", "La Apartada",
            "Momil", "Purísima", "Cotorra", "San Antero",
            "San Bernardo del Viento", "San Carlos", "Valencia", "Canalete",
            "Los Córdobas", "Moñitos", "Tuchín", "Puerto Escondido",
            "San Andrés de Sotavento", "Chimá", "Chima", "Santa Cruz de Lorica"
        );

        List<String> municipiosAntioquia = Arrays.asList(
            "Medellín", "Bello", "Itagüí", "Envigado", "Apartadó",
            "Turbo", "Rionegro", "Caucasia", "Chigorodó", "Carepa",
            "Necoclí", "Arboletes", "San Juan de Urabá", "Mutatá",
            "Vigía del Fuerte", "Murindó", "Dabeiba", "Frontino",
            "Cañasgordas", "Abriaquí", "Buriticá", "Giraldo",
            "Santa Fe de Antioquia", "Olaya", "Sopetrán", "San Jerónimo",
            "Ebéjico", "Heliconia", "Angelópolis"
        );

        final Departamento cordobaFinal = cordoba;
        municipiosCordoba.forEach(nombre -> {
            Municipio m = new Municipio();
            m.setNombre(nombre);
            m.setDepartamento(cordobaFinal);
            m.setDepartamentoNombre("Córdoba");
            municipioRepo.save(m);
        });

        final Departamento antioquiaFinal = antioquia;
        municipiosAntioquia.forEach(nombre -> {
            Municipio m = new Municipio();
            m.setNombre(nombre);
            m.setDepartamento(antioquiaFinal);
            m.setDepartamentoNombre("Antioquia");
            municipioRepo.save(m);
        });
    }
}
