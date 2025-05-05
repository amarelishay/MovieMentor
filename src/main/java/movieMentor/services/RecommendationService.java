package movieMentor.services;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import movieMentor.beans.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final OpenAiService openAiService;


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
}
