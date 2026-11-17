package owewell.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import owewell.application.GroupRepository;
import owewell.application.GroupSummary;
import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.UserId;

public class InMemoryGroupRepository implements GroupRepository {

    private final Map<GroupId, Group> groups = new ConcurrentHashMap<>();
    private final Set<GroupId> deleted = ConcurrentHashMap.newKeySet();

    @Override
    public void save(Group group) {
        groups.put(group.id(), group);
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        if (deleted.contains(id)) {
            return Optional.empty();
        }
        return Optional.ofNullable(groups.get(id));
    }

    @Override
    public List<GroupSummary> findSummariesForMember(UserId member) {
        return groups.values().stream()
            .filter(group -> !deleted.contains(group.id()))
            .filter(group -> group.members().contains(member))
            .map(group -> new GroupSummary(group.id(), group.name()))
            .toList();
    }

    @Override
    public void markDeleted(GroupId id) {
        deleted.add(id);
    }
}
