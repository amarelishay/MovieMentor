package movieMentor.beans;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    private Long id; // זהו ה־TMDB ID

    private String title;

    private LocalDate releaseDate;

    @Column(length = 1000)
    private String overview;

    private String posterUrl;

    private Double popularity;

    private Double voteAverage;

    private Integer voteCount;
}
