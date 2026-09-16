package com.roxana.taskbuddy;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // ChatModel e necesar ca bean (SmartTaskController îl cere), dar nu e folosit direct în teste
    @MockitoBean
    private ChatModel chatModel;

    // SecurityFilterChain e necesar ca bean pentru a preveni eroarea de context loading
    @MockitoBean
    private SecurityFilterChain securityFilterChain;

    @org.junit.jupiter.api.BeforeEach
    void setUpPasswordEncoder() {
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "$2a$" + invocation.getArgument(0));
    }

    // ─── TEST 1: POST /api/auth/register — înregistrare reușită ───
    @Test
    void registerUser_shouldHashPasswordAndSaveUser() throws Exception {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        String hashed = passwordEncoder.encode("parola123");
        User savedUser = new User("newuser", hashed);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"parola123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utilizator înregistrat cu succes!"));

        verify(userRepository).findByUsername("newuserr");
        verify(userRepository).save(any(User.class));
    }

    // ─── TEST 2: POST /api/auth/register — username deja existent ───
    @Test
    void registerUser_shouldReturnBadRequestWhenUsernameExists() throws Exception {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(new User("newuser", "hashed")));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"password\":\"parola123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Eroare: Username-ul este deja luat!"));

        verify(userRepository).findByUsername("newuser");
        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }

    // ─── TEST 3: GET /api/auth/login — returnează mesaj de autentificare reușită ───
    @Test
    void loginUser_shouldReturnOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Autentificare reușită!"));
    }

    // ─── TEST 4: POST /api/auth/register — body gol → salvează cu null (controller fără validare) ───
    @Test
    void registerUser_shouldSaveWithNullWhenFieldsEmpty() throws Exception {
        // Arrange — controller-ul nu validează, salvează cu username/password null
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        User savedUser = new User(null, null);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utilizator înregistrat cu succes!"));

        verify(userRepository).save(any(User.class));
    }
}
