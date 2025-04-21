package movieMentor.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api.key}")
    private String apiKey;

    public List<String> fetchRecommendationsFromChatGPT(List<String> favoriteTitles, List<String> lastWatchedTitles) {
        String prompt = buildPrompt(favoriteTitles, lastWatchedTitles);

        String response = webClientBuilder.build()
                .post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestBody(prompt))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractTitlesFromResponse(response);
    }

    private String buildPrompt(List<String> favorites, List<String> history) {
        return "Based on the following favorite movies: " + String.join(", ", favorites) +
                " and these recently watched movies: " + String.join(", ", history) +
                ". Suggest 10 similar movies. Return only a list of movie names separated by commas.";
    }

    private String buildRequestBody(String prompt) {
        return "{\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"You are a helpful movie recommendation engine.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + prompt + "\"}\n" +
                "  ],\n" +
                "  \"temperature\": 0.7\n" +
                "}";

    }

    private List<String> extractTitlesFromResponse(String response) {
        // פשטות - תחפש את התוכן בין "content": "..." ותחלק לפי פסיקים
        String content = response.split("\\\"content\\\": \\\"", 2)[1].split("\\\"", 2)[0];
        return List.of(content.split(", "));
    }
}
