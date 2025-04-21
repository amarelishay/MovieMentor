package movieMentor.services;

import lombok.RequiredArgsConstructor;
import movieMentor.beans.User;
import movieMentor.dto.AuthResponse;
import movieMentor.dto.LoginRequest;
import movieMentor.dto.RegisterRequest;
import movieMentor.repository.UserRepository;
import movieMentor.security.JwtService;
import movieMentor.security.UserDetailsImpl;
import movieMentor.services.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUsername());

        return new AuthResponse(token);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token);
    }
}
