package owewell.api;

import java.util.Objects;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import owewell.application.UserRepository;
import owewell.domain.User;
import owewell.domain.UserId;

public class AppOidcUserService extends OidcUserService {

    private final UserRepository users;

    public AppOidcUserService(UserRepository users) {
        this.users = users;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser googleUser = super.loadUser(request);   // Spring gets the person's details from Google

        users.save(new User(
            new UserId(googleUser.getSubject()),
            Objects.requireNonNullElse(googleUser.getFullName(), googleUser.getEmail()),
            googleUser.getEmail()));

        return googleUser;
    }
}
