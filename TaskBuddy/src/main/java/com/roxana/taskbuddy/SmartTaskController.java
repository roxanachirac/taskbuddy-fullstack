package com.roxana.taskbuddy;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class SmartTaskController {

    private final ChatModel chatModel;
    private final TaskRepository taskRepository; // Acum se văd direct, fiind în același pachet!

    @Autowired
    public SmartTaskController(ChatModel chatModel, TaskRepository taskRepository) {
        this.chatModel = chatModel;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/subtasks")
    public List<Task> generateAndSaveSubtasks(@RequestParam(value = "task") String task) {

        String systemPrompt = "Ești un manager de proiect expert. Descompune următorul task în 5 sub-task-uri clare, executive și concrete. " +
                "Returnează răspunsul strict ca o lîstă separată prin virgulă, fără numere, fără introducere și fără alte explicații. " +
                "Task-ul este: " + task;

        String aiResponse = chatModel.call(systemPrompt);

        if (aiResponse == null || aiResponse.isBlank()) {
            return List.of();
        }

        List<Task> savedTasks = Arrays.stream(aiResponse.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(subTaskTitle -> new Task(subTaskTitle, "Generat inteligent pentru: " + task, false))
                .toList();

        return taskRepository.saveAll(savedTasks);
    }
}