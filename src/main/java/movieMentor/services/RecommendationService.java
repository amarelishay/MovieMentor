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

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final OpenAiService openAiService; // השירות שיטפל בתקשורת עם ChatGPT

    public List<Movie> generateRecommendations(String username) {
        User user = userRepository.findByUsernameOrEmail(username,username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // נשלוף את 30 הסרטים האחרונים (או פחות אם אין)
        List<String> lastWatchedTitles = user.getWatchHistory().stream()
                .skip(Math.max(0, user.getWatchHistory().size() - 30))
                .map(Movie::getTitle)
                .collect(Collectors.toList());

        List<String> favoriteTitles = user.getFavoriteMovies().stream()
                .map(Movie::getTitle)
                .collect(Collectors.toList());

        // קריאה ל־ChatGPT דרך השירות החיצוני
        List<String> suggestedTitles = openAiService.fetchRecommendationsFromChatGPT(favoriteTitles, lastWatchedTitles);

        // נריץ התאמה מול מסד הנתונים ונעדכן את המשתמש
        List<Movie> recommended = new ArrayList<>();
        for (String title : suggestedTitles) {
            movieRepository.findByTitle(title).ifPresent(recommended::add);

        }

        user.setRecommendedMovies(recommended);
        userRepository.save(user);
        System.out.println("recommended movies updated :"+recommended.toString());
        return recommended;
    }
}
