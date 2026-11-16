package owewell.infrastructure;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import owewell.application.UserRepository;
import owewell.domain.User;
import owewell.domain.UserId;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, User> users = new ConcurrentHashMap<>();

    @Override
    public void save(User user) {
        users.put(user.id(), user);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return Optional.ofNullable(users.get(id));
    }
}
