// RecommendationService.java
package movieMentor.services;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import movieMentor.beans.User;
import movieMentor.repository.MovieRepository;
import movieMentor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final TmdbService tmdbService;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final OpenAiService openAiService;
    public void generateRecommendations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // שמות סרטים מהמועדפים
        List<String> favorites = user.getFavoriteMovies().stream()
                .map(Movie::getTitle)
                .collect(Collectors.toList());

        // 30 הסרטים האחרונים מתוך ההיסטוריה
        List<String> history = user.getWatchHistory().stream()
                .skip(Math.max(0, user.getWatchHistory().size() - 30))
                .map(Movie::getTitle)
                .collect(Collectors.toList());

        // שליחת הנתונים ל־ChatGPT וקבלת שמות סרטים מוצעים
        List<String> recommendedTitles = openAiService.getRecommendations(favorites, history);
        user.getRecommendedMovies().clear();
        List<Movie> movies=new ArrayList<>();
        for (String movieName : recommendedTitles){
            movies.add(tmdbService.searchMovies(movieName).get(0));
        }
        user.setRecommendedMovies(movies);
        System.out.println(user+movies.toString());
        userRepository.save(user);
    }



}
