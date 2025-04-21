package movieMentor.init;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.User;
import movieMentor.repository.UserRepository;
import movieMentor.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(String... args) {
        final String username = "Elishayamar";
        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("🟡 המשתמש כבר קיים: " + username);
            return;
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode("14789510"))
                .email("elishay61@gmail.com")
                .name("Elishay Amar")
                .birthDate(LocalDate.of(1999, 6, 3))
                .build();

        userRepository.save(user);
        System.out.println("✅ נוצר משתמש חדש: " + username);

        // סרטים אהובים
        Arrays.asList("Inception", "Interstellar", "The Dark Knight").forEach(title ->
                safe(() -> userService.addFavoriteMovie(username, title)));

        // היסטוריית צפייה
        Arrays.asList("The Matrix", "Avatar", "Tenet").forEach(title ->
                safe(() -> userService.addToWatchHistory(username, title)));

        System.out.println("🎥 הוזנו סרטים אהובים והיסטוריית צפייה למשתמש " + username);
    }

    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            System.err.println("⚠️ שגיאה בעת הוספה: " + e.getMessage());
        }
    }
}
