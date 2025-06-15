package movieMentor.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMBEDDING_URL = "https://vector-service.onrender.com/embed";

    public float[] getEmbedding(String text) {
        try {
            // בונה את הבקשה
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String json = "{\"text\": \"" + text.replace("\"", "\\\"") + "\"}";
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            // שולח ל־/embed
            ResponseEntity<String> response = restTemplate.postForEntity(EMBEDDING_URL, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // ממיר את התשובה ל־float[]
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode arr = root.get("embedding");

                float[] embedding = new float[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    embedding[i] = (float) arr.get(i).asDouble();
                }

                return embedding;
            } else {
                log.error("❌ Failed to get embedding: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error getting embedding", e);
        }

        return new float[0]; // במקרה של כשל
    }
}
