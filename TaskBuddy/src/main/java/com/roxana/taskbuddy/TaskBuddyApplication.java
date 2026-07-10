package com.roxana.taskbuddy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.roxana.taskbuddy.TaskController.countWordFrequency;

@SpringBootApplication
public class TaskBuddyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskBuddyApplication.class, args);
        List<String> titles = List.of("Invata Java", "Invata React", "Java este cool");
        System.out.println(countWordFrequency(titles));
    }
}

// 1.a ENTITATEA USER NOUĂ
@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    @JsonIgnore // Nu trimitem niciodată parola hashuită în format JSON către React
    private String password;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
//    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
//    public void setPassword(String password) { this.password = password; }
}

// 1.b ENTITATEA (Această clasă va deveni tabela "task" în PostgreSQL)
@Entity
@Table(name = "tasks")
class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID-ul va crește automat (1, 2, 3...) în baza de date
    private Long id;

    @NotBlank(message = "Titlul task-ului nu poate fi gol!")
    private String title;
    private String description;
    private boolean completed;

    // LEGĂTURA: Fiecare task aparține unui singur utilizator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // Evităm buclele infinite la serializarea JSON-ului
    private User user;

    // Hibernate are nevoie de un constructor gol obligatoriu
    public Task() {}

    public Task(String title, String description, boolean completed, User user) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.user = user;
    }

    // Getters și Setters (JPA are nevoie de ei pentru a citi și scrie datele)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

// 2. REPOSITORUL (Interfața care ne dă acces la metode gen save(), findAll(), delete())
@Repository
interface TaskRepository extends JpaRepository<Task, Long> {
}

// INTERFAȚA REPOSITORY PENTRU USER
@Repository
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

// 3. CONTROLLERUL REST (Rămâne creierul API-ului, dar acum vorbește cu baza de date)
@RestController
@RequestMapping("/api/tasks")
class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Spring va injecta automat repositorul aici (Dependency Injection)
    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // Helper util pentru a lua utilizatorul curent rapid și a nu repeta codul
    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu este autentificat!"));
    }

    // GET /api/tasks - Ia toate task-urile din PostgreSQL
    @GetMapping
    public List<Task> getAllTasks(Principal principal) {
        User currentUser = getCurrentUser(principal);
        // Returnăm DOAR task-urile care aparțin utilizatorului logat
        return taskRepository.findAll().stream()
                .filter(task -> task.getUser() != null && task.getUser().getId().equals(currentUser.getId()))
                .toList();
    }

    // POST /api/tasks - Salvează un task nou în PostgreSQL și întoarce lista actualizată
    @PostMapping
    public List<Task> createTask(@Valid @RequestBody Task newTask, Principal principal) {
        User currentUser = getCurrentUser(principal);
        // Asociem noul task cu utilizatorul logat înainte de salvare
        newTask.setUser(currentUser);
        newTask.setCompleted(false); // Forțăm task-ul nou să fie "În lucru"
        taskRepository.save(newTask); // Îl salvăm în baza de date
        return getAllTasks(principal);// Întoarcem lista proaspătă
    }

    // PUT /api/tasks/{id}/toggle - Schimbă starea unui task direct în baza de date
    @PutMapping("/{id}/toggle")
    public List<Task> toggleTask(@PathVariable Long id, Principal principal) {
        User currentUser = getCurrentUser(principal);
        // Căutam task-ul după ID; dacă nu îl găsim, aruncăm o eroare
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost găsit!"));

        // Verificare de securitate: Te asiguri că utilizatorul nu încearcă să modifice task-ul altcuiva!
        if (task.getUser() == null || !task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Eroare: Nu ai permisiunea să modifici acest task!");
        }

        task.setCompleted(!task.isCompleted()); // Inversăm starea
        taskRepository.save(task); // Salvăm modificarea în DB

        return getAllTasks(principal);
    }

    // DELETE /api/tasks/{id} - Șterge un task după ID și întoarce lista rămasă
    @DeleteMapping("/{id}")
    public List<Task> deleteTask(@PathVariable Long id, Principal principal) {
        User currentUser = getCurrentUser(principal);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost găsit!"));

        // Verificare de securitate la ștergere
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
