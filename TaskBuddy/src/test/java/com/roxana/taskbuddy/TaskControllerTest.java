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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskRepository taskRepository;

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

    private User createUserWithId(String username, long id) {
        User user = new User(username, passwordEncoder.encode("pass"));
        user.setId(id);
        return user;
    }

    private MockHttpServletRequestBuilder withMockUser(MockHttpServletRequestBuilder request) {
        return request.with(request1 -> {
            request1.setRemoteUser("testuser");
            request1.setUserPrincipal(new Principal() {
                @Override public String getName() { return "testuser"; }
            });
            return request1;
        });
    }

    // ─── TEST 1: GET /api/tasks — returnează lista goală când nu există task-uri ───
    @Test
    void getAllTasks_shouldReturnEmptyListWhenNoTasks() throws Exception {
        // Arrange
        User mockUser = createUserWithId("testuser", 1L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(withMockUser(get("/api/tasks")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userRepository).findByUsername("testuser");
        verify(taskRepository).findAll();
    }

    // ─── TEST 2: POST /api/tasks — creează un task cu succes ───
    @Test
    void createTask_shouldSaveTaskAndReturnList() throws Exception {
        // Arrange
        User mockUser = createUserWithId("testuser", 1L);
        Task newTask = new Task("Test Task", "Description", false, mockUser);
        newTask.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);
        when(taskRepository.findAll()).thenReturn(List.of(newTask));

        // Act & Assert
        mockMvc.perform(withMockUser(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test Task\",\"description\":\"Description\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].completed").value(false));

        verify(taskRepository).save(any(Task.class));
    }

    // ─── TEST 3: PUT /api/tasks/{id}/toggle — inversează starea task-ului ───
    @Test
    void toggleTask_shouldFlipCompletedStatus() throws Exception {
        // Arrange
        User mockUser = createUserWithId("testuser", 1L);
        Task task = new Task("Task One", "Desc", false, mockUser);
        task.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskRepository.findAll()).thenReturn(List.of(task));

        // Act & Assert
        mockMvc.perform(withMockUser(put("/api/tasks/1/toggle")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completed").value(true));

        verify(taskRepository).save(any(Task.class));
    }

    // ─── TEST 4: DELETE /api/tasks/{id} — șterge un task ───
    @Test
    void deleteTask_shouldRemoveTaskAndReturnRemainingList() throws Exception {
        // Arrange
        User mockUser = createUserWithId("testuser", 1L);
        Task task = new Task("Old Task", "Desc", false, mockUser);
        task.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.findAll()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(withMockUser(delete("/api/tasks/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskRepository).delete(task);
    }
}
