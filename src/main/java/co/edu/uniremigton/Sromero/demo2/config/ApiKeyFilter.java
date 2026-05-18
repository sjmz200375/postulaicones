package co.edu.uniremigton.Sromero.demo2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.api-key}")
    private String expectedApiKey;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicRoute(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                objectMapper.writeValueAsString(Map.of("codigo", 401, "mensaje", "API Key inválida o ausente"))
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicRoute(String path, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        if ("POST".equalsIgnoreCase(method) && "/api/postulaciones".equals(path)) return true;
        if ("POST".equalsIgnoreCase(method) && "/api/usuarios/login".equals(path)) return true;
        if (path.startsWith("/swagger-ui/") || path.equals("/swagger-ui.html")) return true;
        if (path.startsWith("/v3/api-docs/") || path.equals("/v3/api-docs")) return true;
        return false;
    }
}
