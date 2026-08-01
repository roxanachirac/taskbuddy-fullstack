package com.roxana.taskbuddy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public void setId(Long id) { this.id = id; }
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
