package movieMentor.services;

import movieMentor.beans.Movie;
import movieMentor.beans.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void addFavoriteMovie(String username, String movieTitle);

    void addToWatchHistory(String username, String movieTitle);

    void setRecommendedMovies(String username, List<String> recommendedTitles);

    List<Movie> getRecommendations(String username);
}
