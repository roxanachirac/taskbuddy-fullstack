package com.roxana.taskbuddy;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu este autentificat!"));
    }

    // GET /api/tasks - Ia toate task-urile din PostgreSQL
    @GetMapping
    public List<Task> getAllTasks(Principal principal) {
        User currentUser = getCurrentUser(principal);
        return taskRepository.findAll().stream()
                .filter(task -> task.getUser() != null && task.getUser().getId().equals(currentUser.getId()))
                .toList();
    }

    // POST /api/tasks - Salvează un task nou în PostgreSQL și întoarce lista actualizată
    @PostMapping
    public List<Task> createTask(@Valid @RequestBody Task newTask, Principal principal) {
        User currentUser = getCurrentUser(principal);
        newTask.setUser(currentUser);
        newTask.setCompleted(false);
        taskRepository.save(newTask);
        return getAllTasks(principal);
    }

    // PUT /api/tasks/{id}/toggle - Schimbă starea unui task direct în baza de date
    @PutMapping("/{id}/toggle")
    public List<Task> toggleTask(@PathVariable Long id, Principal principal) {
        User currentUser = getCurrentUser(principal);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost găsit!"));

        if (task.getUser() == null || !task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Eroare: Nu ai permisiunea să modifici acest task!");
        }

        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);

        return getAllTasks(principal);
    }

    // DELETE /api/tasks/{id} - Șterge un task după ID și întoarce lista rămasă
    @DeleteMapping("/{id}")
    public List<Task> deleteTask(@PathVariable Long id, Principal principal) {
        User currentUser = getCurrentUser(principal);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost găsit!"));

        if (task.getUser() == null || !task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Eroare: Nu ai permisiunea să ștergi acest task!");
        }

        taskRepository.delete(task);
        return getAllTasks(principal);
    }

    public static Map<String, Integer> countWordFrequency(List<String> titles) {
        Map<String, Integer> wordToCount = new HashMap<>();
        for (String title : titles) {
            String[] words = title.split(" ");
            for (String word : words) {
                String cleanedWord = word.toLowerCase().trim();
                if (cleanedWord.isEmpty()) continue;

                wordToCount.put(cleanedWord, wordToCount.getOrDefault(cleanedWord, 0) +1);
            }
        }
        return wordToCount;
    }
}
