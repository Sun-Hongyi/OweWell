package owewell.application;

import java.util.Optional;

import owewell.domain.Group;
import owewell.domain.GroupId;

//This is the interface for finding and saving groups
public interface GroupRepository {
    void save(Group group);
    Optional<Group> findById(GroupId id);
}
