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

@RestController
@RequestMapping("/api/ai")
class SmartTaskController {

    private final ChatModel chatModel;
    private final TaskRepository taskRepository;

    @Autowired
    public SmartTaskController(ChatModel chatModel, TaskRepository taskRepository) {
        this.chatModel = chatModel;
        this.taskRepository = taskRepository;
    }

    // Definim un Record simplu, local, care acționează ca tipar pentru AI
    // Îi spunem lui OpenAI că vrem exact o listă de titluri numită "titles"
    public record AiTaskResponse(List<String> titles) {}

    /**
     * Generează sub-task-uri folosind Structured Outputs (siguranță maximă la parsare)
     */
    @GetMapping("/subtasks")
    public List<Task> generateAndSaveSubtasks(@RequestParam(value = "task") String task) {

        // 1. Inițializăm convertorul inteligent pentru clasa noastră de tip Record
        BeanOutputConverter<AiTaskResponse> outputConverter = new BeanOutputConverter<>(AiTaskResponse.class);

        // 2. Creăm un șablon de prompt dinamic în care injectăm instrucțiunile de formatare JSON generate automat de Spring AI
        String template = """
                Ești un manager de proiect expert. Descompune următorul task în 5 sub-task-uri clare, executive și concrete.
                Task-ul principal este: {task}
                
                {format}
                """;


        PromptTemplate promptTemplate = new PromptTemplate(template);
        // Injectăm parametrii: task-ul utilizatorului și regulile JSON riguroase din convertor
        Prompt prompt = promptTemplate.create(Map.of(
                "task", task,
                "format", outputConverter.getFormat()
        ));

        // 3. Executăm apelul către OpenAI
        String rawResponse = chatModel.call(prompt).getResult().getOutput().getContent();

        // 4. Convertorul transformă textul primit direct în obiectul nostru Java (AiTaskResponse)
        AiTaskResponse structuredData = outputConverter.convert(rawResponse);

        if (structuredData == null || structuredData.titles() == null) {
            return List.of();
        }

        // 5. Mapăm String-urile sigure în entități Task și le salvăm în PostgreSQL
        List<Task> savedTasks = structuredData.titles().stream()
                .map(title -> new Task(title, "Generat inteligent pentru: " + task, false))
                .toList();

        return taskRepository.saveAll(savedTasks);
    }
}