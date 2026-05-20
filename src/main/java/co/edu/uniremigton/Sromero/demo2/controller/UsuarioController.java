package co.edu.uniremigton.Sromero.demo2.controller;

import co.edu.uniremigton.Sromero.demo2.model.Usuario;
import co.edu.uniremigton.Sromero.demo2.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de cuentas de acceso al sistema. Las contraseñas se almacenan cifradas con BCrypt. POST /login es público.")
public class UsuarioController {

    private final UsuarioService service;

    @Operation(
        summary = "Listar usuarios paginados",
        description = "Retorna usuarios paginados. Parámetros: page (desde 0), size (default 20)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de usuarios",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"content\":[{\"userId\":1,\"userUsername\":\"admin\",\"userRol\":\"ADMIN\",\"userActivo\":true}],\"totalElements\":1,\"totalPages\":1,\"number\":0,\"first\":true,\"last\":true}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente")
    })
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            return ResponseEntity.ok(service.listarPaginado(page, size));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Perfil de usuario por username",
        description = "Retorna los datos básicos de un usuario sin la contraseña. Parámetro requerido: ?username=admin"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"userId\":1,\"username\":\"admin\",\"rol\":\"ADMIN\",\"tercId\":\"\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente")
    })
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(
            @Parameter(description = "Username del usuario") @RequestParam String username) {
        try {
            return ResponseEntity.ok(service.buscarPorUsername(username));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Crear usuario del sistema",
        description = "Crea una nueva cuenta de acceso al panel admin. " +
            "La contraseña se cifra automáticamente con BCrypt. " +
            "El rol por defecto es ADMIN si no se especifica."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"userId\":2,\"userUsername\":\"coordinador1\",\"userPassword\":\"$2a$10$...\",\"userRol\":\"ADMIN\",\"userActivo\":true,\"tercId\":null}"))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"El username es requerido\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "409", description = "El nombre de usuario ya existe",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"El nombre de usuario ya existe\"}")))
    })
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario u) {
        try {
            if (u.getUserUsername() == null || u.getUserUsername().isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", true, "mensaje", "El username es requerido"));
            }
            if (u.getUserPassword() == null || u.getUserPassword().isBlank()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", true, "mensaje", "La contraseña es requerida"));
            }
            return ResponseEntity.ok(service.crear(u));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", true, "mensaje", "El nombre de usuario ya existe"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Actualizar usuario",
        description = "Modifica los datos de un usuario. " +
            "Si se envía userPassword, se cifra automáticamente con BCrypt. " +
            "Si no se envía userPassword (o viene vacío), la contraseña actual no cambia."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"userId\":2,\"userUsername\":\"coordinador1\",\"userRol\":\"ADMIN\",\"userActivo\":true,\"tercId\":null}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Usuario no encontrado: 99\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @Parameter(description = "ID del usuario a actualizar") @PathVariable Long id,
            @RequestBody Usuario u) {
        try {
            return ResponseEntity.ok(service.actualizar(id, u));
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Error";
            HttpStatus status = msg.contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
            return ResponseEntity.status(status).body(Map.of("error", true, "mensaje", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina una cuenta de acceso del sistema. No elimina el Tercero vinculado si lo tiene."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":false,\"mensaje\":\"Eliminado\"}"))),
        @ApiResponse(responseCode = "401", description = "API Key inválida o ausente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @Parameter(description = "ID del usuario a eliminar") @PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok(Map.of("error", false, "mensaje", "Eliminado"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }

    @Operation(
        summary = "Iniciar sesión",
        description = "Endpoint público. Autentica al usuario con username y password. " +
            "Devuelve los datos del usuario para guardar en sesión. " +
            "La contraseña se verifica con BCrypt. No requiere X-API-Key."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"userId\":1,\"username\":\"admin\",\"rol\":\"ADMIN\",\"tercId\":\"\"}"))),
        @ApiResponse(responseCode = "400", description = "Campos requeridos ausentes",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"username y password son requeridos\"}"))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas o usuario inactivo",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\":true,\"mensaje\":\"Credenciales invalidas\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", true, "mensaje", "username y password son requeridos"));
            }
            Map<String, Object> resultado = service.login(username, password);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", true, "mensaje", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", true, "mensaje", "Error interno del servidor"));
        }
    }
}
