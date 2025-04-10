package ch.uzh.ifi.hase.soprafs24.filters;

import ch.uzh.ifi.hase.soprafs24.service.UserService;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.context.annotation.Profile;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@CrossOrigin(origins = "*")
@Component
@Profile("!test")  // The filter will not be loaded when the "test" profile is active
public class AuthenticationFilter extends OncePerRequestFilter {
    private final String[][] publicPaths = {
            {"/users/login", null}, //null = all
            {"/users", "POST"}
    };

    private final UserService userService;

    @Autowired
    public AuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private UserRepository userRepository;

    private boolean isPublicPath(String path, String method) {
        for (String[] config : publicPaths) {
            if (path.startsWith(config[0]) && (config[1] == null || config[1].equals(method))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("AuthenticationFilter: " + request.getRequestURI() + " " + request.getMethod());

        // Handle WebSocket connections with token authentication
        if (request.getRequestURI().startsWith("/ws")) {
            String token = request.getParameter("token");
            
            // Check if token exists and is valid
            if (token == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"WebSocket authentication token required\"}");
                response.setContentType("application/json");
                return;
            }
            
            // Verify token against repository
            if (userRepository.findByToken(token) == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid WebSocket authentication token\"}");
                response.setContentType("application/json");
                return;
            }
            
            // Token is valid, allow connection
            filterChain.doFilter(request, response);
            return;
        }

        // Add CORS headers to ALL responses
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Access-Control-Allow-Origin");
        response.setHeader("Access-Control-Max-Age", "3600");

        String path = request.getRequestURI();
        String method = request.getMethod();

        // For OPTIONS requests, just return with the CORS headers
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Important: don't continue with filter chain
        }

        if (isPublicPath(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Authentication required\"}");
            response.setContentType("application/json");
            return;
        }

        String token = authHeader.substring(7);

        if (userRepository.findByToken(token) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid authentication token\"}");
            response.setContentType("application/json");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
