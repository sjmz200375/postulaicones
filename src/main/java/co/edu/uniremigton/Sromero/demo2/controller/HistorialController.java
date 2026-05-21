package co.edu.uniremigton.Sromero.demo2.controller;

import co.edu.uniremigton.Sromero.demo2.service.HistorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
@Tag(name = "Historial", description = "Registro de cambios de estado de postulaciones y terceros. Requiere X-API-Key.")
public class HistorialController {

    private final HistorialService service;

    @Operation(
        summary = "Listar historial de cambios",
        description = "Retorna el historial paginado. Filtros opcionales: " +
            "tipo (POSTULACION|TERCERO), username, desde (yyyy-MM-dd), hasta (yyyy-MM-dd), " +
            "page (desde 0), size (default 20)"
    )
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            return ResponseEntity.ok(
                service.filtrar(tipo, username, desde, hasta, page, size));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }
}
