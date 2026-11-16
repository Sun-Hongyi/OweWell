package owewell.application;

import owewell.domain.GroupId;

public class ConcurrentUpdateException extends RuntimeException {
    public ConcurrentUpdateException(GroupId id) {
        super("Group " + id.value() + " was updated by someone else. Please reload and try again.");
    }
}
