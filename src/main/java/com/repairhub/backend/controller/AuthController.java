```java
package com.repairhub.backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repairhub.backend.entity.Admin;
import com.repairhub.backend.repository.AdminRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = {
        "http://localhost:5173",
        "http://localhost:5174"
    },
    allowCredentials = "true"
)
public class AuthController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            SecurityContextRepository securityContextRepository) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
    }

    private ResponseEntity<Map<String, Object>> response(
            HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        if (request.username == null || request.password == null
                || request.username.isBlank()
                || request.password.isBlank()) {
            return response(HttpStatus.BAD_REQUEST,
                    "Username/email and password are required.");
        }

        String identifier = request.username.trim();

        Optional<Admin> foundAdmin =
                adminRepository.findByUsername(identifier);

        if (foundAdmin.isEmpty()) {
            foundAdmin = adminRepository.findByEmail(identifier);
        }

        if (foundAdmin.isEmpty()
                || !passwordEncoder.matches(
                    request.password, foundAdmin.get().getPassword())) {
            return response(HttpStatus.UNAUTHORIZED,
                    "Invalid username/email or password.");
        }

        Admin admin = foundAdmin.get();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                    admin.getUsername(),
                    null,
                    List.of(
                        new org.springframework.security.core.authority
                            .SimpleGrantedAuthority("ROLE_ADMIN")
                    )
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        securityContextRepository.saveContext(
                context, httpRequest, httpResponse);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Login successful.");
        body.put("username", admin.getUsername());

        return ResponseEntity.ok(body);
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> session() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String)
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return response(HttpStatus.UNAUTHORIZED, "Not logged in.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("loggedIn", true);
        body.put("username", authentication.getName());

        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(
                Map.of("message", "Logout successful."));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody RegisterRequest request) {

        if (request.username == null || request.email == null
                || request.password == null
                || request.username.isBlank()
                || request.email.isBlank()
                || request.password.isBlank()) {
            return response(HttpStatus.BAD_REQUEST,
                    "Username, email and password are required.");
        }

        String username = request.username.trim();
        String email = request.email.trim().toLowerCase();

        if (username.length() < 3) {
            return response(HttpStatus.BAD_REQUEST,
                    "Username must contain at least 3 characters.");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return response(HttpStatus.BAD_REQUEST,
                    "Enter a valid email address.");
        }

        if (request.password.length() < 8) {
            return response(HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters.");
        }

        if (adminRepository.findByUsername(username).isPresent()) {
            return response(HttpStatus.CONFLICT,
                    "Username already exists.");
        }

        if (adminRepository.findByEmail(email).isPresent()) {
            return response(HttpStatus.CONFLICT,
                    "Email already exists.");
        }

        Admin admin = new Admin(
                username, email, passwordEncoder.encode(request.password));
        adminRepository.save(admin);

        return response(HttpStatus.CREATED,
                "Admin account registered successfully.");
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String)
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return response(HttpStatus.UNAUTHORIZED,
                    "Please log in first.");
        }

        if (request.currentPassword == null || request.newPassword == null
                || request.currentPassword.isBlank()
                || request.newPassword.isBlank()) {
            return response(HttpStatus.BAD_REQUEST,
                    "Current password and new password are required.");
        }

        if (request.newPassword.length() < 8) {
            return response(HttpStatus.BAD_REQUEST,
                    "New password must contain at least 8 characters.");
        }

        Optional<Admin> foundAdmin =
                adminRepository.findByUsername(authentication.getName());

        if (foundAdmin.isEmpty()) {
            return response(HttpStatus.NOT_FOUND,
                    "Admin account not found.");
        }

        Admin admin = foundAdmin.get();

        if (!passwordEncoder.matches(
                request.currentPassword, admin.getPassword())) {
            return response(HttpStatus.BAD_REQUEST,
                    "Current password is incorrect.");
        }

        if (passwordEncoder.matches(
                request.newPassword, admin.getPassword())) {
            return response(HttpStatus.BAD_REQUEST,
                    "Choose a password different from your current password.");
        }

        admin.setPassword(passwordEncoder.encode(request.newPassword));
        adminRepository.save(admin);

        return response(HttpStatus.OK,
                "Password changed successfully. Please log in again.");
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
    }

    public static class ChangePasswordRequest {
        public String currentPassword;
        public String newPassword;
    }
}
```