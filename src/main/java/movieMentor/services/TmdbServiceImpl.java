package movieMentor.services;

import movieMentor.beans.Movie;
import lombok.RequiredArgsConstructor;
import movieMentor.models.MovieSearchResponse;
import movieMentor.models.TmdbMovie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriUtils;


import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service // מסמן ל־Spring שזה שירות
public class TmdbServiceImpl implements TmdbService {
    @Value("${tmdb.api.token}") // קורא את הטוקן מה־application.properties
    private String apiToken;

    @Value("${tmdb.api.base-url}") // כתובת הבסיס של TMDB
    private String apiBaseUrl;
    @Autowired
    private final RestTemplate restTemplate = new RestTemplate(); // כלי לשליחת בקשות HTTP

    // פונקציה שמחזירה 15 סרטים שמוקרנים כעת בקולנוע
    @Override
    public List<Movie> getNowPlayingMovies() {
        String url = apiBaseUrl + "/movie/now_playing?language=en-US&page=1&region=IL";
        return fetchMoviesFromTmdb(url);
    }

    // פונקציה שמחזירה 15 סרטים עם הדירוגים הגבוהים ביותר
    @Override
    public List<Movie> getTopRatedMovies() {
        String url = apiBaseUrl + "/movie/top_rated?language=en-US&page=1";
        return fetchMoviesFromTmdb(url);
    }

    @Override
    public List<Movie> searchMovies(String query) {
        String url = apiBaseUrl + "/search/movie?language=en-US&page=1&query=" + UriUtils.encode(query, StandardCharsets.UTF_8);

        return fetchMoviesFromTmdb(url);
    }

    // פונקציה כללית לשליפת סרטים מ־TMDB

    private List<Movie> fetchMoviesFromTmdb(String url) {
        // יצירת headers עם הטוקן שלך
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken); // חייב להיות Bearer + רווח + טוקן
        headers.set("Accept", "application/json");

        // עטיפת headers בתוך HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // שליחת הבקשה ל-TMDB
        ResponseEntity<MovieSearchResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, MovieSearchResponse.class
        );
        System.out.println(response);

        // בדיקה שהתשובה תקינה ויש גוף
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<TmdbMovie> tmdbResults = response.getBody().getResults();

            List<Movie> movieList = new ArrayList<>();

            // עובר על עד 15 סרטים
            for (int i = 0; i < Math.min(15, tmdbResults.size()); i++) {
                TmdbMovie movieResult = tmdbResults.get(i);

                // ממיר TmdbMovie ל-Entity Movie
                Movie movie = Movie.builder()
                        .id(movieResult.getId())
                        .title(movieResult.getTitle())
                        .overview(movieResult.getOverview())
                        .posterUrl("https://image.tmdb.org/t/p/w500" + movieResult.getPosterPath())
                        .releaseDate(parseDate(movieResult.getReleaseDate()))
                        .popularity(movieResult.getPopularity())
                        .voteAverage(movieResult.getVoteAverage())
                        .voteCount(movieResult.getVoteCount())
                        .build();

                movieList.add(movie);
            }

            return movieList;
        }

        // במקרה של שגיאה – רשימה ריקה
        return new ArrayList<>();
    }



    // ממיר מחרוזת תאריך ל־LocalDate
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
