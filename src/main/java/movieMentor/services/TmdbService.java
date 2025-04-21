package movieMentor.services;

import movieMentor.beans.Movie;

import java.util.List;

public interface TmdbService {
    List<Movie> getNowPlayingMovies();      // סרטים בקולנוע עכשיו
    List<Movie> getTopRatedMovies();        // סרטים אהובים בכל הזמנים
    public List<Movie> searchMovies(String query);// חיפוש

}
