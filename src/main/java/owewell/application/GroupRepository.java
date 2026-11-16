package owewell.application;

import java.util.List;
import java.util.Optional;

import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.UserId;

// This is the interface for finding and saving groups
public interface GroupRepository {
    void save(Group group);
    Optional<Group> findById(GroupId id);
    List<GroupSummary> findSummariesForMember(UserId member);
}
