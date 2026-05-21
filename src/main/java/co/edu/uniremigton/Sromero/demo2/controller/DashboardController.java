package co.edu.uniremigton.Sromero.demo2.controller;

import co.edu.uniremigton.Sromero.demo2.model.EstadoPostulacion;
import co.edu.uniremigton.Sromero.demo2.model.TipoPostulacion;
import co.edu.uniremigton.Sromero.demo2.repository.PostulacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Estadísticas generales del sistema de postulaciones. Requiere X-API-Key.")
public class DashboardController {

    private final PostulacionRepository postulacionRepo;

    @Operation(
        summary = "Obtener estadísticas del dashboard",
        description = "Retorna conteos de postulaciones por estado (total, pendientes, aprobadas, rechazadas) " +
            "y por tipo (ESTUDIANTE, PROFESOR, ADMINISTRATIVO). Requiere X-API-Key."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas correctamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"total\":25,\"pendientes\":10,\"aprobadas\":12,\"rechazadas\":3,\"porTipo\":{\"ESTUDIANTE\":15,\"PROFESOR\":7,\"ADMINISTRATIVO\":3}}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        try {
            long total           = postulacionRepo.count();
            long pendientes      = postulacionRepo.countByEstado(EstadoPostulacion.PENDIENTE);
            long aprobadas       = postulacionRepo.countByEstado(EstadoPostulacion.APROBADA);
            long rechazadas      = postulacionRepo.countByEstado(EstadoPostulacion.RECHAZADA);
            long estudiantes     = postulacionRepo.countByTipo(TipoPostulacion.ESTUDIANTE);
            long profesores      = postulacionRepo.countByTipo(TipoPostulacion.PROFESOR);
            long administrativos = postulacionRepo.countByTipo(TipoPostulacion.ADMINISTRATIVO);

            Map<String, Object> porTipo = new LinkedHashMap<>();
            porTipo.put("ESTUDIANTE",     estudiantes);
            porTipo.put("PROFESOR",       profesores);
            porTipo.put("ADMINISTRATIVO", administrativos);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("total",      total);
            stats.put("pendientes", pendientes);
            stats.put("aprobadas",  aprobadas);
            stats.put("rechazadas", rechazadas);
            stats.put("porTipo",    porTipo);

            return ResponseEntity.ok(stats);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }
}
