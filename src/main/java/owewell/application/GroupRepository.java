package owewell.application;

import java.util.List;
import java.util.Optional;

import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.UserId;

// This is the interface for finding and saving groups
public interface GroupRepository {
    void save(Group group);

    // Deleted groups are never returned
    Optional<Group> findById(GroupId id);

    // Deleted groups are left out
    List<GroupSummary> findSummariesForMember(UserId member);

    // Soft delete: hides the group everywhere, but keeps its history in storage
    void markDeleted(GroupId id);
}
