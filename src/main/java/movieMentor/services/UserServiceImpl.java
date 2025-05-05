package movieMentor.services;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import movieMentor.beans.User;
import movieMentor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TmdbService tmdbService;
    private final RecommendationService recommendationService;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @Transactional
    @CacheEvict(value = "userRecommendations", key = "#username")

    public void addFavoriteMovie(String username, String movieTitle) {
        User user = fetchUser(username);
        Movie movie = tmdbService.getOrCreateMovie(movieTitle);
        boolean added = user.getFavoriteMovies().add(movie);

        if (added) {
            logger.info("✅ Added movie '{}' to favorites for user '{}'", movieTitle, username);
            updateRecommendations(user);
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
            if (user.getWatchHistory().size() % 5 == 0) {
                logger.info("📊 Triggering recommendation update — history count divisible by 5");
                updateRecommendations(user);
            }
        }
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
        List<String> newTitles = recommendationService.generateRecommendations(user);
        List<Movie> updated = tmdbService.updateMovieListWithDifferences(user.getRecommendedMovies(), newTitles);
        user.setRecommendedMovies(updated);
        userRepository.saveAndFlush(user);
        logger.info("🔁 Automatically updated recommendations for '{}'", user.getUsername());
    }

    private User fetchUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + usernameOrEmail));
    }
}
