package movieMentor.services;


import movieMentor.beans.Movie;

import java.util.List;

public interface MovieService {

    // מחזיר את כל הסרטים במסד
    List<Movie> getAllMovies();

    // מחפש סרט לפי ID
    Movie getMovieById(Long id);
    Movie getOrCreateMovie(String title);

    // שומר סרט (למשל מתוך TMDB)
    Movie addMovie(Movie movie);

    // מוחק סרט לפי ID
    void deleteMovie(Long id);
}
