package owewell.infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import owewell.application.GroupRepository;
import owewell.domain.Group;
import owewell.domain.GroupId;

public class InMemoryGroupRepository implements GroupRepository {

    private final Map<GroupId, Group> groups = new HashMap<>();

    @Override
    public void save(Group group) {
        groups.put(group.id(), group);
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return Optional.ofNullable(groups.get(id));
    }
}
