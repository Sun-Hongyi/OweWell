package owewell.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import owewell.application.GroupRepository;
import owewell.application.GroupSummary;
import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.UserId;

public class InMemoryGroupRepository implements GroupRepository {

    private final Map<GroupId, Group> groups = new ConcurrentHashMap<>();

    @Override
    public void save(Group group) {
        groups.put(group.id(), group);
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return Optional.ofNullable(groups.get(id));
    }

    @Override
    public List<GroupSummary> findSummariesForMember(UserId member) {
        return groups.values().stream()
            .filter(group -> group.members().contains(member))
            .map(group -> new GroupSummary(group.id(), group.name()))
            .toList();
    }
}
