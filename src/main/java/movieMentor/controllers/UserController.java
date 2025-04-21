package movieMentor.controllers;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.Movie;
import movieMentor.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. הוספת סרט למועדפים
    @PostMapping("/favorites/{title}")
    public ResponseEntity<?> addFavorite(@PathVariable String title, Authentication auth) {
        userService.addFavoriteMovie(auth.getName(), title);
        return ResponseEntity.ok("✔️ סרט נוסף למועדפים");
    }

    // 2. הוספת סרט להיסטוריית צפייה
    @PostMapping("/history/{title}")
    public ResponseEntity<?> addToHistory(@PathVariable String title, Authentication auth) {
        userService.addToWatchHistory(auth.getName(), title);
        return ResponseEntity.ok("👁️ נוסף להיסטוריית צפייה");
    }

    // 3. שליחת המלצות (תקבל רשימה של כותרים מהלקוח, לדוגמה מה-AI)
    @PostMapping("/recommendations")
    public ResponseEntity<?> updateRecommendations(@RequestBody List<String> titles, Authentication auth) {
        userService.setRecommendedMovies(auth.getName(), titles);
        return ResponseEntity.ok("✅ רשימת ההמלצות עודכנה");
    }

    // 4. שליפת המלצות נוכחיות
    @GetMapping("/recommendations")
    public ResponseEntity<List<Movie>> getRecommendations(Authentication auth) {
        return ResponseEntity.ok(userService.getRecommendations(auth.getName()));
    }
}
