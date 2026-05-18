package co.edu.uniremigton.Sromero.demo2.controller;

import co.edu.uniremigton.Sromero.demo2.model.Tercero;
import co.edu.uniremigton.Sromero.demo2.service.TerceroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/terceros")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Terceros", description = "Gestión de personas aprobadas dentro del sistema. Los Terceros se crean automáticamente al aprobar una postulación. Todos los endpoints requieren X-API-Key.")
public class TerceroController {

    private final TerceroService service;

    @Operation(
        summary = "Listar terceros",
        description = "Retorna todos los terceros registrados en el sistema (personas aprobadas). " +
            "Filtra por tipo con ?tipo=0 (Estudiante), 1 (Profesor), 2 (Administrativo). " +
            "Busca por nombre, apellido o número de documento con ?nombre=texto."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "[{\"tercId\":201,\"tercTipoDoc\":\"CC\",\"tercNroDoc\":\"1065000100\",\"tercNombres\":\"JUAN CARLOS\",\"tercApellidos\":\"PEREZ GOMEZ\",\"tercDireccion\":null,\"tercTelefono\":\"3001234567\",\"tercMovil\":null,\"tercTipo\":\"0\",\"tercEmail\":\"juan@correo.com\"}]"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<?> listar(
            @Parameter(description = "Nombre, apellido o número de documento a buscar") @RequestParam(required = false) String nombre,
            @Parameter(description = "Tipo: 0=Estudiante, 1=Profesor, 2=Administrativo") @RequestParam(required = false) String tipo) {
        try {
            List<Tercero> resultado;
            if (tipo != null) {
                resultado = service.listarPorTipo(tipo);
            } else if (nombre != null && !nombre.isBlank()) {
                resultado = service.buscar(nombre);
            } else {
                resultado = service.listar();
            }
            return ResponseEntity.ok(resultado);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", ex.getMessage() != null ? ex.getMessage() : "Error"));
        }
    }

    @Operation(
        summary = "Obtener tercero por ID",
        description = "Retorna los datos completos de un tercero. " +
            "El tercId es el identificador que se usará a futuro para asignación de cursos y registro de notas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tercero encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"tercId\":201,\"tercTipoDoc\":\"CC\",\"tercNroDoc\":\"1065000100\",\"tercNombres\":\"JUAN CARLOS\",\"tercApellidos\":\"PEREZ GOMEZ\",\"tercDireccion\":null,\"tercTelefono\":\"3001234567\",\"tercMovil\":null,\"tercTipo\":\"0\",\"tercEmail\":\"juan@correo.com\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Tercero no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Tercero no encontrado\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> porId(
            @Parameter(description = "ID del tercero") @PathVariable Long id) {
        try {
            Optional<Tercero> t = service.porId(id);
            if (t.isPresent()) return ResponseEntity.ok(t.get());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", true, "mensaje", "Tercero no encontrado"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", ex.getMessage() != null ? ex.getMessage() : "Error"));
        }
    }

    @Operation(
        summary = "Actualizar datos del tercero",
        description = "Permite corregir datos de un tercero ya aprobado (teléfono, email, dirección, etc.). " +
            "Esta operación NO afecta la postulación original — " +
            "postulación y tercero son registros independientes (snapshot histórico)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tercero actualizado exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"tercId\":201,\"tercNombres\":\"JUAN CARLOS\",\"tercApellidos\":\"PEREZ GOMEZ\",\"tercTipo\":\"0\",\"tercEmail\":\"nuevo@correo.com\",\"tercTelefono\":\"3109999999\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Tercero no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Tercero no encontrado: 99\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @Parameter(description = "ID del tercero a actualizar") @PathVariable Long id,
            @RequestBody Tercero t) {
        try {
            return ResponseEntity.ok(service.actualizar(id, t));
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Error";
            HttpStatus status = msg.contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("error", true, "mensaje", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Eliminar tercero",
        description = "Elimina el tercero del sistema. " +
            "IMPORTANTE: al eliminar, la postulación original vinculada vuelve automáticamente al estado PENDIENTE " +
            "con tercId = null, permitiendo que el admin la re-evalúe."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tercero eliminado y postulación reseteada a PENDIENTE",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":false,\"mensaje\":\"Eliminado correctamente\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Tercero no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Tercero no encontrado: 99\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @Parameter(description = "ID del tercero a eliminar") @PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok(Map.of("error", false, "mensaje", "Eliminado correctamente"));
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Error";
            HttpStatus status = msg.contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(Map.of("error", true, "mensaje", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }
}
