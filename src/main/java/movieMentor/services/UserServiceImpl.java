package movieMentor.services;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import movieMentor.beans.User;
import movieMentor.repository.MovieRepository;
import movieMentor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final MovieService movieService;

    @Override
    public void addFavoriteMovie(String username, String movieTitle) {
        User user = fetchUser(username);
        Movie movie = movieService.getOrCreateMovie(movieTitle);

        boolean added = user.getFavoriteMovies().add(movie);
        if (added) {
            userRepository.save(user); // רק אם נוספה חדשה
            recommendationService.generateRecommendations(username);
        }
    }

    @Override
    public void addToWatchHistory(String username, String movieTitle) {
        User user = fetchUser(username);
        Movie movie = movieService.getOrCreateMovie(movieTitle);

        boolean added = user.getWatchHistory().add(movie);
        if (added) {
            userRepository.save(user);
            // אם נרצה שהוספה לצפייה תוביל לעדכון המלצות, אפשר להוסיף תנאי כאן
            if (user.getWatchHistory().size() % 3 == 0) {
                recommendationService.generateRecommendations(username);
            }
        }
    }

    @Override
    public void setRecommendedMovies(String username, List<String> recommendedTitles) {
        User user = fetchUser(username);
        user.getRecommendedMovies().clear();

        recommendedTitles.stream()
                .limit(15)
                .map(this::safeGetMovie)
                .filter(movie -> movie != null)
                .forEach(user.getRecommendedMovies()::add);

        userRepository.save(user);
    }

    @Override
    public List<Movie> getRecommendations(String username) {
        return fetchUser(username).getRecommendedMovies();
    }

    // ----- עזר -----

    private User fetchUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username) // מאפשר חיפוש גם לפי אימייל
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }




    private Movie safeGetMovie(String title) {
        try {
            return movieService.getOrCreateMovie(title);
        } catch (Exception e) {
            System.err.println("⚠️ שגיאה בשליפת הסרט: " + title);
            return null;
        }
    }
}
