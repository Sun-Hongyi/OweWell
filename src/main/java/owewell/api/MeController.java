package owewell.api;

import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    @GetMapping("/me")
    public Map<String, String> me(@AuthenticationPrincipal OidcUser user) {
        return Map.of(
            "id", user.getSubject(),
            "name", Objects.requireNonNullElse(user.getFullName(), ""),
            "email", Objects.requireNonNullElse(user.getEmail(), ""));
    }
}
