package com.roxana.taskbuddy.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/ai")
public class SmartTaskController {

    private final ChatModel chatModel;

    // Injectăm nativ clientul de chat oferit de Spring AI
    @Autowired
    public SmartTaskController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Endpoint care primește un task global și folosește AI pentru a-l sparge în sub-task-uri simple.
     * Exemplu apel: GET /api/ai/subtasks?task=Organizeaza o conferinta tech
     */
    @GetMapping("/subtasks")
    public List<String> generateSubtasks(@RequestParam(value = "task") String task) {

        // Definim un prompt riguros, cerându-i modelului să ne returneze doar liniile separate prin virgulă
        String systemPrompt = "Ești un manager de proiect expert. Descompune următorul task în 5 sub-task-uri clare, executive și concrete. " +
                "Returnează răspunsul strict ca o listă separată prin virgulă, fără numere, fără introducere și fără alte explicații. " +
                "Task-ul este: " + task;

        // Apelăm modelul OpenAI (implicit va folosi gpt-4o sau modelul configurat standard de Spring AI)
        String aiResponse = chatModel.call(systemPrompt);

        // Parsăm răspunsul primit din format text într-o listă curată de String-uri
        if (aiResponse == null || aiResponse.isBlank()) {
            return List.of("Nu s-au putut genera sub-task-uri.");
        }

        return Arrays.stream(aiResponse.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}