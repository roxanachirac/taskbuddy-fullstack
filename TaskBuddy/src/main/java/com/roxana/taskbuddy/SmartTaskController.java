package com.roxana.taskbuddy;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.security.Principal;

@RestController
@RequestMapping("/api/ai")
class SmartTaskController {

    private final ChatModel chatModel;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public SmartTaskController(ChatModel chatModel, TaskRepository taskRepository, UserRepository userRepository) {
        this.chatModel = chatModel;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public record AiTaskResponse(List<String> titles) {}

    // --- STRUCTURI NOI PENTRU PRIORITIZARE ---
    public record PrioritizedTaskItem(Long id, String title, int priorityOrder, String justification) {}
    public record AiPrioritizationResponse(List<PrioritizedTaskItem> prioritizedTasks) {}

    /**
     * Endpoint 1: Generează sub-task-uri folosind Structured Outputs și le salvează în DB
     */
    @GetMapping("/subtasks")
    public List<Task> generateAndSaveSubtasks(@RequestParam(value = "task") String task, Principal principal) {
        // Găsim utilizatorul logat curent în baza de date
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu este logat"));
        BeanOutputConverter<AiTaskResponse> outputConverter = new BeanOutputConverter<>(AiTaskResponse.class);

        String template = """
                Ești un manager de proiect expert. Descompune următorul task în 5 sub-task-uri clare, executive și concrete.
                Task-ul principal este: {task}
                
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("task", task, "format", outputConverter.getFormat()));

        String rawResponse = chatModel.call(prompt).getResult().getOutput().getContent();
        AiTaskResponse structuredData = outputConverter.convert(rawResponse);

        if (structuredData == null || structuredData.titles() == null) {
            return List.of();
        }

        List<Task> savedTasks = structuredData.titles().stream()
                .map(title -> new Task(title, "Generat inteligent pentru: " + task, false, currentUser)) // ◄ Trimis direct în constructor
                .toList();

        return taskRepository.saveAll(savedTasks);
    }

    /**
     * Endpoint 2: Citește toate task-urile active din DB și le ordonează inteligent cu AI
     */
    @GetMapping("/prioritize")
    public AiPrioritizationResponse prioritizeExistingTasks(Principal principal) {
        // 1. Luăm toate task-urile din baza de date și le filtrăm doar pe cele nefinalizate
        List<Task> activeTasks = taskRepository.findAll().stream()
                .filter(task -> task.getUser() != null && task.getUser().getUsername().equals(principal.getName())) // ◄ Filtrare după user
                .filter(task -> !task.isCompleted())
                .toList();

        if (activeTasks.isEmpty()) {
            return new AiPrioritizationResponse(List.of());
        }

        // 2. Formatăm lista de task-uri ca un text simplu pe care AI-ul să îl poată citi cu ușurință
        String tasksString = activeTasks.stream()
                .map(t -> "ID: " + t.getId() + " - Titlu: " + t.getTitle())
                .collect(Collectors.joining("\n"));

        // 3. Pregătim convertorul structurat pentru răspunsul de prioritizare
        BeanOutputConverter<AiPrioritizationResponse> outputConverter = new BeanOutputConverter<>(AiPrioritizationResponse.class);

        String template = """
                Ești un asistent de productivitate de nivel Senior executiv.
                Mai jos este o listă cu task-urile curente ale utilizatorului extrase din baza de date.
                Analizează-le și ordonează-le în funcție de dependențele lor logice și importanță (priorityOrder de la 1 la N, unde 1 este prioritatea maximă).
                Oferă o justificare scurtă de maximum o propoziție în limba română pentru fiecare alegere.
                
                Task-urile curente:
                {tasks}
                
                {format}
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("tasks", tasksString, "format", outputConverter.getFormat()));

        // 4. Apelăm modelul AI
        String rawResponse = chatModel.call(prompt).getResult().getOutput().getContent();

        // 5. Convertim răspunsul text într-un obiect complet structurat și îl returnăm
        return outputConverter.convert(rawResponse);
    }
}