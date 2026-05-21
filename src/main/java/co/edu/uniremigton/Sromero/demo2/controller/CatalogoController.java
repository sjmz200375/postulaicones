package co.edu.uniremigton.Sromero.demo2.controller;

import co.edu.uniremigton.Sromero.demo2.model.*;
import co.edu.uniremigton.Sromero.demo2.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
@Tag(name = "Catálogos", description = "Gestión de Programas Académicos, Áreas/Departamentos y Cargos")
public class CatalogoController {

    private final CatalogoService service;

    @Operation(summary = "Listar programas académicos")
    @GetMapping("/programas")
    public ResponseEntity<?> listarProgramas(
            @RequestParam(defaultValue = "false") boolean soloActivos) {
        try {
            return ResponseEntity.ok(service.listarProgramas(soloActivos));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Crear programa académico")
    @PostMapping("/programas")
    public ResponseEntity<?> crearPrograma(@Valid @RequestBody ProgramaAcademico p) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crearPrograma(p));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Actualizar programa académico")
    @PutMapping("/programas/{id}")
    public ResponseEntity<?> actualizarPrograma(
            @PathVariable Long id, @Valid @RequestBody ProgramaAcademico p) {
        try {
            return ResponseEntity.ok(service.actualizarPrograma(id, p));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Eliminar programa académico")
    @DeleteMapping("/programas/{id}")
    public ResponseEntity<?> eliminarPrograma(@PathVariable Long id) {
        try {
            service.eliminarPrograma(id);
            return ResponseEntity.ok(Map.of("error", false, "mensaje", "Eliminado correctamente"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Listar áreas o departamentos")
    @GetMapping("/areas")
    public ResponseEntity<?> listarAreas(
            @RequestParam(defaultValue = "false") boolean soloActivos) {
        try {
            return ResponseEntity.ok(service.listarAreas(soloActivos));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Crear área o departamento")
    @PostMapping("/areas")
    public ResponseEntity<?> crearArea(@Valid @RequestBody AreaDepartamento a) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crearArea(a));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Actualizar área o departamento")
    @PutMapping("/areas/{id}")
    public ResponseEntity<?> actualizarArea(
            @PathVariable Long id, @Valid @RequestBody AreaDepartamento a) {
        try {
            return ResponseEntity.ok(service.actualizarArea(id, a));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Eliminar área o departamento")
    @DeleteMapping("/areas/{id}")
    public ResponseEntity<?> eliminarArea(@PathVariable Long id) {
        try {
            service.eliminarArea(id);
            return ResponseEntity.ok(Map.of("error", false, "mensaje", "Eliminado correctamente"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Listar cargos")
    @GetMapping("/cargos")
    public ResponseEntity<?> listarCargos(
            @RequestParam(defaultValue = "false") boolean soloActivos) {
        try {
            return ResponseEntity.ok(service.listarCargos(soloActivos));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Crear cargo")
    @PostMapping("/cargos")
    public ResponseEntity<?> crearCargo(@Valid @RequestBody Cargo c) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crearCargo(c));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Actualizar cargo")
    @PutMapping("/cargos/{id}")
    public ResponseEntity<?> actualizarCargo(
            @PathVariable Long id, @Valid @RequestBody Cargo c) {
        try {
            return ResponseEntity.ok(service.actualizarCargo(id, c));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(summary = "Eliminar cargo")
    @DeleteMapping("/cargos/{id}")
    public ResponseEntity<?> eliminarCargo(@PathVariable Long id) {
        try {
            service.eliminarCargo(id);
            return ResponseEntity.ok(Map.of("error", false, "mensaje", "Eliminado correctamente"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }
}
