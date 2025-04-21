package movieMentor.beans;

import lombok.*;
import movieMentor.beans.Movie;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique = true)
    @Email
    private  String email;
    @Column(unique = true)
    private String username;
    private  String password;
    private LocalDate birthDate;

    // סרטים שהמשתמש אהב

    @JoinTable(
            name = "user_favorite_movies",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Movie> favoriteMovies = new ArrayList<>();


    // היסטוריית צפייה
    @JoinTable(
            name = "user_watch_history",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Movie> watchHistory = new ArrayList<>();
    // המלצות – עד 15 סרטים, עם סדר
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "user_recommendations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    @OrderColumn(name = "recommendation_order")
    private List<Movie> recommendedMovies;

    public User(String name, String email, String password, LocalDate birthDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
    }
}
