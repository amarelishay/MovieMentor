package movieMentor.services;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import movieMentor.beans.User;
import movieMentor.enums.TopMoviesData;
import movieMentor.models.MovieImage;
import movieMentor.repository.UserRepository;
import movieMentor.utils.EmbeddingUtils;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private final OpenAiService openAiService;
    private final TmdbService tmdbService;
    private final EmbeddingService embeddingService;
    private final EmbeddingStorageService embeddingStorageService;
    private final UserSimilarityService userSimilarityService;
    private final UserRepository userRepository;

    public List<String> generateRecommendations(User user) {
        List<String> favoriteTitles = user.getFavoriteMovies().stream()
                .map(Movie::getTitle)
                .collect(Collectors.toList());

        List<String> historyTitles = user.getWatchHistory().stream()
                .skip(Math.max(0, user.getWatchHistory().size() - 30))
                .map(Movie::getTitle)
                .collect(Collectors.toList());

        return openAiService.getRecommendations(favoriteTitles, historyTitles);
    }

    public List<Movie> prepareCandidateMovies() {
        List<Movie> topRatedRaw = tmdbService.getTopRatedMovies();
        List<Movie> nowPlayingRaw = tmdbService.getNowPlayingMovies();

        List<Movie> candidates = new ArrayList<>();

        try {
            for (Movie movie : topRatedRaw) {
                candidates.add(movie);
            }

            for (Movie movie : nowPlayingRaw) {
                candidates.add(movie);
            }
            TopMoviesData[] topMovies = TopMoviesData.values();
            for (TopMoviesData movie : topMovies) {
                Movie addedMovie = tmdbService.getOrCreateMovie(movie.getTitle());
                candidates.add(addedMovie);
                if (!embeddingStorageService.hasEmbedding(addedMovie.getId())) {
                    logger.info("movie {} was added from enum", movie.getTitle());
                    embeddingStorageService.addEmbedding(addedMovie.getId(), embeddingService.getEmbedding(movie.getDescription()));
                }
            }
        } catch (Exception e) {
            logger.error("❌ Error processing candidate movies", e);
        }

        return candidates;
    }


    @SuppressWarnings("unchecked")
    private Movie convertMapToMovie(Object obj) {
        try {
            Map<String, Object> map = (Map<String, Object>) obj;

            Movie movie = new Movie();
            movie.setId(Long.valueOf((Integer) map.get("id")));
            movie.setTitle((String) map.get("title"));
            movie.setOriginalTitle((String) map.get("originalTitle"));
            movie.setOverview((String) map.get("overview"));
            movie.setPosterUrl((String) map.get("posterUrl"));
            movie.setReleaseDate(LocalDate.parse((String) map.get("releaseDate")));
            movie.setPopularity(((Number) map.get("popularity")).doubleValue());
            movie.setVoteAverage(((Number) map.get("voteAverage")).doubleValue());
            movie.setVoteCount(((Number) map.get("voteCount")).intValue());

            // תרגום imageUrls (רשימת Map)
            List<Map<String, String>> imageMaps = (List<Map<String, String>>) map.get("imageUrls");
            if (imageMaps != null) {
                List<MovieImage> images = imageMaps.stream()
                        .map(m -> new MovieImage(m.get("type"), m.get("url")))
                        .collect(Collectors.toList());
                movie.setImageUrls(images);
            }

            return movie;
        } catch (Exception e) {
            logger.warn("⚠️ Failed to convert map to Movie: {}", obj, e);
            return null;
        }
    }


    public List<Movie> findMostSimilarMovies(float[] userVector, List<Movie> candidateMovies, int topN) {
        if (userVector == null || userVector.length == 0) {
            throw new IllegalArgumentException("User embedding is missing");
        }

        List<Map.Entry<Movie, Double>> scored = new ArrayList<>();

        for (Movie movie : candidateMovies) {
            float[] vector = embeddingStorageService.getEmbedding(movie.getId());
            if (vector != null && vector.length == userVector.length) {
                double similarity = EmbeddingUtils.cosineSimilarity(userVector, vector);
                scored.add(Map.entry(movie, similarity));
            }
        }

        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        return scored.stream()
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    public List<Movie> getRecommendationsFromSimilarUsers(User user, int topUsers) {
        List<Map<String, Object>> similarUsers = userSimilarityService.findUsersWithSimilarTaste(user, topUsers);
        Map<String, Integer> movieFrequency = new HashMap<>();

        for (Map<String, Object> userMeta : similarUsers) {
            String userId = (String) userMeta.get("user_id");
            Optional<User> similarUser = userRepository.findById(Long.parseLong(userId));

            similarUser.ifPresent(u -> {
                for (Movie fav : u.getFavoriteMovies()) {
                    movieFrequency.merge(fav.getTitle(), 1, Integer::sum);
                }
            });
        }

        // מיון הסרטים לפי שכיחות והמרה ל־Movie
        List<String> topTitles = movieFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return tmdbService.updateMovieListWithDifferences(user.getRecommendedMovies(), topTitles);
    }

}
