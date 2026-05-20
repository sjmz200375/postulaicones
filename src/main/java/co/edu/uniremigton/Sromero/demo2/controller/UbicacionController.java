package co.edu.uniremigton.Sromero.demo2.controller;

import co.edu.uniremigton.Sromero.demo2.service.UbicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ubicaciones")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Ubicaciones", description = "Departamentos y municipios de Colombia")
public class UbicacionController {

    private final UbicacionService service;

    @Operation(summary = "Listar departamentos disponibles")
    @GetMapping("/departamentos")
    public ResponseEntity<?> listarDepartamentos() {
        try {
            return ResponseEntity.ok(service.listarDepartamentos());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Listar municipios por departamento")
    @GetMapping("/municipios")
    public ResponseEntity<?> listarMunicipios(
            @RequestParam(required = false) Long departamentoId) {
        try {
            if (departamentoId != null) {
                return ResponseEntity.ok(
                    service.listarMunicipiosPorDepartamento(departamentoId));
            }
            return ResponseEntity.ok(service.listarTodosMunicipios());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }
}
