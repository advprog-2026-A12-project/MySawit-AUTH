package id.ac.ui.cs.advprog.auth.service.utils;

import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import java.security.SecureRandom;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsernameGenerator {

    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateUniqueUsername(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-", "")
                .replaceAll("-$", "");

        String candidate;
        do {
            byte[] bytes = new byte[2];
            secureRandom.nextBytes(bytes);
            String suffix = HexFormat.of().formatHex(bytes);
            candidate = slug + "-" + suffix;
            if (candidate.length() > 50) {
                candidate = candidate.substring(0, 50);
            }
        } while (userRepository.existsByUsername(candidate));

        return candidate;
    }
}
