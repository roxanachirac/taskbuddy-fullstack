package com.roxana.taskbuddy;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.roxana.taskbuddy.TaskController.countWordFrequency;

@SpringBootApplication
public class TaskBuddyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskBuddyApplication.class, args);
        List<String> titles = List.of("Invata Java", "Invata React", "Java este cool");
        System.out.println(countWordFrequency(titles));
    }
}

// 1. ENTITATEA (Această clasă va deveni tabela "task" în PostgreSQL)
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

    // Hibernate are nevoie de un constructor gol obligatoriu
    public Task() {}

    public Task(String title, String description, boolean completed) {
        this.title = title;
        this.description = description;
        this.completed = completed;
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
}

// 2. REPOSITORUL (Interfața care ne dă acces la metode gen save(), findAll(), delete())
@Repository
interface TaskRepository extends JpaRepository<Task, Long> {
}

// 3. CONTROLLERUL REST (Rămâne creierul API-ului, dar acum vorbește cu baza de date)
@RestController
@RequestMapping("/api/tasks")
class TaskController {

    private final TaskRepository taskRepository;

    // Spring va injecta automat repositorul aici (Dependency Injection)
    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // GET /api/tasks - Ia toate task-urile din PostgreSQL
    @GetMapping
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    // POST /api/tasks - Salvează un task nou în PostgreSQL și întoarce lista actualizată
    @PostMapping
    public List<Task> addTask(@Valid @RequestBody Task newTask) {
        newTask.setCompleted(false); // Forțăm task-ul nou să fie "În lucru"
        taskRepository.save(newTask); // Îl salvăm în baza de date
        return taskRepository.findAll(); // Întoarcem lista proaspătă
    }

    // PUT /api/tasks/{id}/toggle - Schimbă starea unui task direct în baza de date
    @PutMapping("/{id}/toggle")
    public List<Task> toggleTask(@PathVariable Long id) {
        // Căutam task-ul după ID; dacă nu îl găsim, aruncăm o eroare
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost găsit!"));

        task.setCompleted(!task.isCompleted()); // Inversăm starea
        taskRepository.save(task); // Salvăm modificarea în DB

        return taskRepository.findAll();
    }

    // DELETE /api/tasks/{id} - Șterge un task după ID și întoarce lista rămasă
    @DeleteMapping("/{id}")
    public List<Task> deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id); // JPA șterge automat rândul din PostgreSQL
        return taskRepository.findAll(); // Întoarcem lista actualizată
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
