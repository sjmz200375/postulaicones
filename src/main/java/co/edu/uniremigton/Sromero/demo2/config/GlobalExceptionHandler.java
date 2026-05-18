package co.edu.uniremigton.Sromero.demo2.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("codigo", 400);
        respuesta.put("mensaje", "Datos de entrada inválidos");
        respuesta.put("errores", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleEnumInvalido(HttpMessageNotReadableException ex) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("codigo", 400);
        respuesta.put("mensaje", "Valor inválido en el body. Verifique los enums: " +
            "tipoPostulacion (ESTUDIANTE|PROFESOR|ADMINISTRATIVO), " +
            "estado (PENDIENTE|APROBADA|RECHAZADA)");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}
