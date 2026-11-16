package owewell.application;

import java.util.Optional;

import owewell.domain.User;
import owewell.domain.UserId;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(UserId id);
}
