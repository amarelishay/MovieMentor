package movieMentor.services;


import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import movieMentor.repository.MovieRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MovieServiceImpl implements MovieService {

    private final TmdbService tmdbService;

    @Autowired
    private MovieRepository movieRepository;



    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

    }

    @Override
    public Movie addMovie(Movie movie) {
        movieRepository.save(movie);
        return movie;
    }
    public Movie getOrCreateMovie(String title) {
        return tmdbService.searchMovies(title).stream()
                .findFirst()
                .map(movie ->
                        movieRepository.findByTitle(movie.getTitle())
                                .orElseGet(() -> addMovie(movie))
                )
                .orElseThrow(() -> new RuntimeException("Movie not found: " + title));
    }

    @Override
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}
