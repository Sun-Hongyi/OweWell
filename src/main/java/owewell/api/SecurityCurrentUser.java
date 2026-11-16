package owewell.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import owewell.domain.UserId;

public class SecurityCurrentUser implements CurrentUser {

    @Override
    public UserId id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof OidcUser user)) {
            throw new IllegalStateException("No signed-in user");
        }
        return new UserId(user.getSubject());   // Google's permanent ID
    }
}
