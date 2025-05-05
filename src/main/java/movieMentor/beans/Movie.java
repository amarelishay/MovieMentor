/**
 * Movie Entity - Created by Elishay Amar
 *
 * Represents a movie fetched from TMDB, including basic details, list of images, and trailer URL.
 */

package movieMentor.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import movieMentor.models.MovieImage;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie implements Serializable {

    @Id
    private Long id;

    private String title;
    private String originalTitle;

    @Column(length = 2000)
    private String overview;

    private String posterUrl;

    private LocalDate releaseDate;

    private Double popularity;

    private Double voteAverage;

    private Integer voteCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_images", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "image_url")
    private List<MovieImage> imageUrls;


    private String trailerUrl;

    @Singular
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id"))
    private Set<Actor> actors = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie)) return false;
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
