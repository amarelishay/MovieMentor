package movieMentor.services;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import movieMentor.beans.User;
import movieMentor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final TmdbService tmdbService;
    private final RecommendationService recommendationService;
    private final EmbeddingService embeddingService;
    private final EmbeddingStorageService embeddingStorageService;
    private final UserVectorClientService userVectorClient;

    @Override
    @Transactional
    @CacheEvict(value = "userRecommendations", key = "#username")

    public void addFavoriteMovie(String username, String movieTitle) {
        User user = fetchUser(username);
        Movie movie = tmdbService.getOrCreateMovie(movieTitle);
        boolean added = user.getFavoriteMovies().add(movie);

        if (added) {
            logger.info("✅ Added movie '{}' to favorites for user '{}'", movieTitle, username);
            updateUserContextInVectorDB(user);  // ← עדכון FAISS
            updateRecommendations(user);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "userRecommendations", key = "#username")

    public void removeFavoriteMovie(String username, Long movieId) {
        User user = fetchUser(username);
        boolean removed = user.getFavoriteMovies().removeIf(m -> m.getId().equals(movieId));

        if (removed) {
            logger.info("🗑️ Removed movie ID {} from favorites for user '{}'", movieId, username);
            updateRecommendations(user);
            updateUserContextInVectorDB(user);  // ← עדכון FAISS
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "userRecommendations", key = "#username")
    public void addToWatchHistory(String username, String movieTitle) {
        User user = fetchUser(username);
        Movie movie = tmdbService.getOrCreateMovie(movieTitle);
        boolean added = user.getWatchHistory().add(movie);

        if (added) {
            logger.info("🎬 Added '{}' to watch history of '{}'", movieTitle, username);
            if (!embeddingStorageService.hasEmbedding(movie.getId())) {
                float[] vector = embeddingService.getEmbedding(movie.getOverview());
                embeddingStorageService.addEmbedding(movie.getId(), vector);
            }
            if (user.getWatchHistory().size() % 5 == 0) {
                logger.info("📊 Triggering recommendation update — history count divisible by 5");
                updateRecommendations(user);
            }
        }
        updateUserContextInVectorDB(user);  // ← עדכון FAISS

    }
    private float[] buildUserProfileEmbeddingWeighted(User user) {
        // הגדרת המשקלים
        final float FAVORITE_WEIGHT = 2.0f;
        final float HISTORY_WEIGHT = 1.0f;
        final int HISTORY_LIMIT = 30;

        List<float[]> vectors = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        // מועדפים
        for (Movie movie : user.getFavoriteMovies()) {
            float[] vector = embeddingStorageService.getEmbedding(movie.getId());
            if (vector != null && vector.length > 0) {
                vectors.add(vector);
                weights.add(FAVORITE_WEIGHT);
            }

        }

        // היסטוריית צפייה – רק 30 אחרונים
        List<Movie> history = user.getWatchHistory();
        int start = Math.max(0, history.size() - HISTORY_LIMIT);
        List<Movie> recentHistory = history.subList(start, history.size());

        for (Movie movie : recentHistory) {
            float[] vector = embeddingStorageService.getEmbedding(movie.getId());
            if (vector != null && vector.length > 0) {
                vectors.add(vector);
                weights.add(HISTORY_WEIGHT);
            }
        }

        if (vectors.isEmpty()) {
            logger.warn("⚠️ No valid embeddings found for user '{}'", user.getUsername());
            return new float[0];
        }

        int dim = vectors.get(0).length;
        float[] weightedSum = new float[dim];
        float totalWeight = 0;

        for (int v = 0; v < vectors.size(); v++) {
            float[] vec = vectors.get(v);
            float weight = weights.get(v);
            totalWeight += weight;

            for (int i = 0; i < dim; i++) {
                weightedSum[i] += vec[i] * weight;
            }
        }

        for (int i = 0; i < dim; i++) {
            weightedSum[i] /= totalWeight;
        }

        logger.info("✅ Built weighted profile embedding for '{}'", user.getUsername());
        return weightedSum;
    }

    @Cacheable(value = "userRecommendations", key = "#username")
    @Override
    public List<Movie> getRecommendations(String username) {
        return fetchUser(username).getRecommendedMovies();
    }

    @Cacheable(value = "userFavorites", key = "#username")
    @Override
    public List<Movie> getFavorites(String username) {
        return fetchUser(username).getFavoriteMovies();
    }

    @Override
    public List<Movie> getHistory(String username) {
        return fetchUser(username).getWatchHistory();
    }

    @Override
    @Transactional
    @CacheEvict(value = "userRecommendations", key = "#username")
    public void setRecommendedMovies(String username, List<String> recommendedTitles) {
        User user = fetchUser(username);
        List<Movie> updated = tmdbService.updateMovieListWithDifferences(user.getRecommendedMovies(), recommendedTitles);
        user.setRecommendedMovies(updated);
        userRepository.saveAndFlush(user);
        logger.info("🛠️ Manually updated recommended movies for '{}'", username);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userRecommendations", key = "#username")
    public void updateRecommendations(User user) {
        float[] userVector = buildUserProfileEmbeddingWeighted(user);
        if (userVector.length == 0) {
            logger.warn("⚠️ User '{}' has no embedding data – skipping vector-based recommendations", user.getUsername());
        }
        List<Movie> candidateMovies = recommendationService.prepareCandidateMovies();
        List<Movie> similarMovies = new ArrayList<>();
        if (userVector.length > 0) {
            similarMovies = recommendationService.findMostSimilarMovies(userVector, candidateMovies, 10);
            logger.info("🎯 Found {} vector-based similar movies for '{}'", similarMovies.size(), user.getUsername());
        }

        List<String> newTitles = recommendationService.generateRecommendations(user);
        List<Movie> updatedGptList = tmdbService.updateMovieListWithDifferences(user.getRecommendedMovies(), newTitles);
        List<Movie> similarTasteMovies = recommendationService.getRecommendationsFromSimilarUsers(user, 5);
        // 5. מיזוג חכם (למשל, לשלב או להעדיף אחד מהמקורות)
        Set<Movie> finalRecommendations = new LinkedHashSet<>();
        logger.info(" similar movies based on vector embedding '{}'", similarMovies.stream().map(Movie::getTitle).collect(Collectors.toList()));
        finalRecommendations.addAll(similarMovies);
        logger.info(" similar movies based on chat GPT '{}'", updatedGptList.stream().map(Movie::getTitle).collect(Collectors.toList()));
        finalRecommendations.addAll(updatedGptList);
        logger.info(" similar movies based on other users '{}'", similarTasteMovies.stream().map(Movie::getTitle).collect(Collectors.toList()));
        finalRecommendations.addAll(similarTasteMovies);
        // 6. עדכון המשתמש ושמירה
        user.setRecommendedMovies(new ArrayList<>(finalRecommendations));
        userRepository.saveAndFlush(user);
        logger.info(" user recommendations '{}'", finalRecommendations.stream().map(Movie::getTitle).collect(Collectors.toList()));
        logger.info("✅ Updated recommended movies for '{}'", user.getUsername());

    }

    private User fetchUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private void updateUserContextInVectorDB(User user) {
        // ודא שכל הסרטים במועדפים/היסטוריה מכילים embedding
        List<Movie> allMovies = new ArrayList<>();
        allMovies.addAll(user.getFavoriteMovies());
        allMovies.addAll(user.getWatchHistory());

        for (Movie movie : allMovies) {
            if (!embeddingStorageService.hasEmbedding(movie.getId())) {
                float[] vector = embeddingService.getEmbedding(movie.getOverview());
                if (vector.length > 0) {
                    embeddingStorageService.addEmbedding(movie.getId(), vector);
                }
            }
        }

        float[] userVector = buildUserProfileEmbeddingWeighted(user);

        if (userVector.length == 0) {
            logger.warn("⛔ User '{}' has empty vector – skipping FAISS update", user.getUsername());
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("favorite_count", user.getFavoriteMovies().size());
        metadata.put("watch_history_count", user.getWatchHistory().size());
        metadata.put("username", user.getUsername());

        userVectorClient.storeUserVector(user.getId().toString(), userVector, metadata);
    }
    public List<Map<String, Object>> findUsersWithSimilarTaste(User user, int topK) {
        float[] userVector = buildUserProfileEmbeddingWeighted(user);
        if (userVector.length == 0) {
            logger.warn("⛔ Cannot find similar users – empty vector for '{}'", user.getUsername());
            return List.of();
        }
        return userVectorClient.findSimilarUsers(userVector, topK);
    }


}
