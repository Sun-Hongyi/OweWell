package owewell.application;

import owewell.domain.GroupId;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException(GroupId id) {
        super("No group with id " + id.value());
    }
}
