package com.teachengineering.springai;

import com.teachengineering.springai.model.Filme;
import com.teachengineering.springai.tools.MovieTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/assistant/chat")
public class AssistantController {

    @Value("classpath:/prompts/system-message.st")
    private Resource systemResource;

    private final ChatClient chatClient;

    @GetMapping
    public String chat(@RequestParam String conversationId, @RequestParam String message) {
        return chatClient.prompt()
                         .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                         .user(message)
                         .call()
                         .content();
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public Flux<String> stream(@RequestParam String conversationId, @RequestParam String message) {
        return chatClient.prompt()
                         .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                         .user(message)
                         .stream()
                         .content();
    }

    @GetMapping("/movie")
    public String movie(
            @RequestParam(required = false) String conversationId,
            @RequestParam String genero,
            @RequestParam String diretor)
    {
        String conversationIdToUse = conversationId != null ? conversationId : UUID.randomUUID()
                                                                                   .toString();
        return chatClient.prompt()
                         .user(u -> u.text("Recomende 3 filmes de {genero} do diretor {diretor}")
                                     .param("genero", genero)
                                     .param("diretor", diretor))
                         .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationIdToUse))
                         .call()
                         .content();
    }

    @GetMapping("/system-resource")
    public String movieSystemResource(
            @RequestParam String conversationId,
            @RequestParam String genero,
            @RequestParam String diretor) throws IOException {

        String systemText = new String(
                systemResource.getInputStream().readAllBytes()).replace(
                        "{contexto_adicional}",
                "Sempre liste apenas filmes lançados nos últimos 5 anos, caso não exista diga isso!");

        return chatClient.prompt()
                         .system(systemText)
                         .user(u ->
                                 u.text("Recomende 3 filmes de {genero} do diretor {diretor}")
                                  .param("genero", genero)
                                  .param("diretor", diretor)
                         )
                         .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                         .call()
                         .content();
    }

    @GetMapping("/recommendation-one-movie")
    public Filme recommendationOne(@RequestParam String conversationId, @RequestParam String genero, @RequestParam String diretor) {
        return chatClient.prompt()
                         .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                         .user(u ->
                                 u.text("""
                                          Recomende 1 filme de {genero} do diretor {diretor}.
                                        """)
                                  .param("genero", genero)
                                  .param("diretor", diretor)
                         )
                         .call()
                         .entity(Filme.class, ChatClient.EntityParamSpec::useProviderStructuredOutput);
    }

    @GetMapping("/recommendation-three-movies")
    public List<Filme> recommendationThree(@RequestParam String conversationId, @RequestParam String genero, @RequestParam String diretor) {
        ResponseEntity<ChatResponse, List<Filme>> chatResponseListResponseEntity = chatClient.prompt()
                                                                                             .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                                                                                             .user(u ->
                                                                                                     u.text("""
                                                                                                                Recomende 3 filmes de {genero} do diretor {diretor}
                                                                                                                Responda APENAS em JSON com campos titulo, ano.
                                                                                                              """)
                                                                                                      .param("genero", genero)
                                                                                                      .param("diretor", diretor)
                                                                                             )
                                                                                             .call()
                                                                                             .responseEntity(new ParameterizedTypeReference<List<Filme>>() {
                                                                                             });

        ChatResponse response = chatResponseListResponseEntity.getResponse();
        if (response != null) {
            int tokens = response.getMetadata().getUsage().getTotalTokens();
            log.atInfo().log("Total tokens: {}", tokens);
        }

        return chatResponseListResponseEntity.getEntity();
    }

    @GetMapping("/movie/tool")
    public String tool(@RequestParam String conversationId, @RequestParam String message) {
        return chatClient
                .prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(u -> u.text(message))
                .tools(new MovieTools())
                .call()
                .content();
    }

}
