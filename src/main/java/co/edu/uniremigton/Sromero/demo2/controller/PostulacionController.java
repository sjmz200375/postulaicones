package co.edu.uniremigton.Sromero.demo2.controller;

import co.edu.uniremigton.Sromero.demo2.model.EstadoPostulacion;
import co.edu.uniremigton.Sromero.demo2.model.Postulacion;
import co.edu.uniremigton.Sromero.demo2.model.TipoPostulacion;
import co.edu.uniremigton.Sromero.demo2.service.PostulacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/postulaciones")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Postulaciones", description = "Gestión de postulaciones institucionales. POST /api/postulaciones es público. Los demás endpoints requieren X-API-Key.")
public class PostulacionController {

    private final PostulacionService service;

    @Operation(
        summary = "Crear postulación",
        description = "Endpoint público. Cualquier persona puede postularse como ESTUDIANTE, PROFESOR o ADMINISTRATIVO. " +
            "El estado inicial siempre es PENDIENTE. No se puede postular dos veces al mismo tipo con el mismo número de documento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Postulación creada exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"id\":1,\"nombres\":\"JUAN CARLOS\",\"apellidos\":\"PEREZ GOMEZ\",\"tipoDoc\":\"CC\",\"nroDoc\":\"1065000100\",\"email\":\"juan@correo.com\",\"telefono\":\"3001234567\",\"tipoPostulacion\":\"ESTUDIANTE\",\"comentarios\":\"Deseo estudiar ingenieria\",\"estado\":\"PENDIENTE\",\"tercId\":null,\"fechaCreacion\":\"2026-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"codigo\":400,\"mensaje\":\"Datos de entrada invalidos\",\"errores\":{\"email\":\"Email invalido\",\"nombres\":\"Los nombres son obligatorios\"}}"))),
        @ApiResponse(responseCode = "409", description = "Ya existe una postulación con ese documento para el tipo seleccionado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Ya existe una postulacion con ese documento para el tipo seleccionado\"}")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Postulacion postulacion) {
        try {
            Postulacion creada = service.crear(postulacion);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Listar postulaciones",
        description = "Retorna todas las postulaciones ordenadas por fecha de creación descendente. " +
            "Parámetros opcionales: estado (PENDIENTE|APROBADA|RECHAZADA), " +
            "tipo (ESTUDIANTE|PROFESOR|ADMINISTRATIVO), texto (busca en nombres, apellidos y nroDoc). " +
            "Se pueden combinar: ?estado=PENDIENTE&tipo=ESTUDIANTE"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de postulaciones",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "[{\"id\":1,\"nombres\":\"JUAN CARLOS\",\"apellidos\":\"PEREZ GOMEZ\",\"tipoDoc\":\"CC\",\"nroDoc\":\"1065000100\",\"email\":\"juan@correo.com\",\"tipoPostulacion\":\"ESTUDIANTE\",\"estado\":\"PENDIENTE\",\"tercId\":null,\"fechaCreacion\":\"2026-01-15T10:30:00\"}]"))),
        @ApiResponse(responseCode = "400", description = "Valor de filtro inválido (estado o tipo desconocido)",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Valor de filtro invalido: ...\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente")
    })
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String texto) {
        try {
            List<Postulacion> resultado;

            if (texto != null && !texto.isBlank()) {
                resultado = service.buscar(texto.trim());
            } else if (estado != null && tipo != null) {
                EstadoPostulacion e = EstadoPostulacion.valueOf(estado.toUpperCase());
                TipoPostulacion t = TipoPostulacion.valueOf(tipo.toUpperCase());
                resultado = service.listarPorEstadoYTipo(e, t);
            } else if (estado != null) {
                EstadoPostulacion e = EstadoPostulacion.valueOf(estado.toUpperCase());
                resultado = service.listarPorEstado(e);
            } else if (tipo != null) {
                TipoPostulacion t = TipoPostulacion.valueOf(tipo.toUpperCase());
                resultado = service.listarPorTipo(t);
            } else {
                resultado = service.listar();
            }

            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", true, "mensaje", "Valor de filtro inválido: " + ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Obtener postulación por ID",
        description = "Retorna los datos completos de una postulación específica incluyendo su estado y tercId si fue aprobada."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Postulación encontrada",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"id\":1,\"nombres\":\"JUAN CARLOS\",\"apellidos\":\"PEREZ GOMEZ\",\"tipoDoc\":\"CC\",\"nroDoc\":\"1065000100\",\"email\":\"juan@correo.com\",\"tipoPostulacion\":\"ESTUDIANTE\",\"estado\":\"APROBADA\",\"tercId\":201,\"fechaCreacion\":\"2026-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Postulación no encontrada",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Postulacion no encontrada\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> porId(@PathVariable Long id) {
        try {
            Optional<Postulacion> opt = service.porId(id);
            if (opt.isPresent()) {
                return ResponseEntity.ok(opt.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", "Postulación no encontrada"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Eliminar postulación",
        description = "Elimina una postulación PENDIENTE o RECHAZADA. " +
            "Las postulaciones APROBADAS no se pueden eliminar desde aquí — " +
            "primero debe eliminarse el Tercero asociado desde el módulo de Terceros."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Eliminado correctamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":false,\"mensaje\":\"Eliminado correctamente\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Postulación no encontrada",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Postulacion no encontrada: 99\"}"))),
        @ApiResponse(responseCode = "409", description = "Postulación aprobada — eliminar desde módulo de Terceros",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Una postulacion aprobada no puede eliminarse aqui. Elimine el tercero asociado en el modulo de Terceros.\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok(Map.of("error", false, "mensaje", "Eliminado correctamente"));
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Error";
            HttpStatus status = msg.contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
            return ResponseEntity.status(status).body(Map.of("error", true, "mensaje", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Aprobar postulación",
        description = "Aprueba una postulación PENDIENTE o RECHAZADA. " +
            "Automáticamente crea un registro en Terceros con los datos del postulante y guarda el tercId en la postulación. " +
            "Mapeo de tipo: ESTUDIANTE→tercTipo=0, PROFESOR→1, ADMINISTRATIVO→2. " +
            "Una postulación ya APROBADA no puede volver a aprobarse."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Postulación aprobada y Tercero creado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"id\":1,\"nombres\":\"JUAN CARLOS\",\"apellidos\":\"PEREZ GOMEZ\",\"tipoPostulacion\":\"ESTUDIANTE\",\"estado\":\"APROBADA\",\"tercId\":201,\"fechaCreacion\":\"2026-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Postulación no encontrada",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Postulacion no encontrada: 99\"}"))),
        @ApiResponse(responseCode = "409", description = "La postulación ya está aprobada",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"La postulacion ya esta aprobada\"}")))
    })
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id) {
        try {
            Postulacion aprobada = service.aprobar(id);
            return ResponseEntity.ok(aprobada);
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Error";
            HttpStatus status = msg.contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
            return ResponseEntity.status(status).body(Map.of("error", true, "mensaje", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Rechazar postulación",
        description = "Rechaza una postulación PENDIENTE. " +
            "Las postulaciones APROBADAS no se pueden rechazar desde aquí — " +
            "deben gestionarse eliminando el Tercero en el módulo de Terceros."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Postulación rechazada exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"id\":1,\"nombres\":\"JUAN CARLOS\",\"apellidos\":\"PEREZ GOMEZ\",\"tipoPostulacion\":\"ESTUDIANTE\",\"estado\":\"RECHAZADA\",\"tercId\":null,\"fechaCreacion\":\"2026-01-15T10:30:00\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Postulación no encontrada",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Postulacion no encontrada: 99\"}"))),
        @ApiResponse(responseCode = "409", description = "No se puede rechazar una postulación aprobada",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Una postulacion aprobada no puede rechazarse aqui. Elimine el tercero asociado en el modulo de Terceros.\"}")))
    })
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Long id) {
        try {
            Postulacion rechazada = service.rechazar(id);
            return ResponseEntity.ok(rechazada);
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Error";
            HttpStatus status = msg.contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
            return ResponseEntity.status(status).body(Map.of("error", true, "mensaje", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }
}
