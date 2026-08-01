package com.roxana.taskbuddy;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Eroare: Username-ul este deja luat!"));
        }

        // HASHUIM parola înainte de salvare în baza de date! Regulă critică de securitate.
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, hashedPassword);
        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of("message", "Utilizator înregistrat cu succes!"));
    }

    @GetMapping("/login")
    public ResponseEntity<?> loginUser() {
        // Dacă cererea ajunge aici, înseamnă că filtrul HTTP Basic a validat deja utilizatorul cu succes
        return ResponseEntity.ok(Map.of("message", "Autentificare reușită!"));
    }
}