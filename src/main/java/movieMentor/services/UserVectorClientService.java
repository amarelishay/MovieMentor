package movieMentor.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserVectorClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://vector-service.onrender.com";

    public void storeUserVector(String userId, float[] embedding, Map<String, Object> metadata) {
        Map<String, Object> request = new HashMap<>();
        request.put("user_id", userId);
        request.put("embedding", embedding);
        request.put("metadata", metadata);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        System.out.println("Embedding length: " + embedding.length);
        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/store_user_vector", entity, String.class);
        System.out.println("🧠 Store Vector: " + response.getBody());


    }

    public List<Map<String, Object>> findSimilarUsers(float[] embedding, int topK) {
        Map<String, Object> request = new HashMap<>();
        request.put("embedding", embedding);
        request.put("top_k", topK);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/find_similar_users",
                HttpMethod.POST,
                entity,
                List.class
        );

        return response.getBody();
    }
}
