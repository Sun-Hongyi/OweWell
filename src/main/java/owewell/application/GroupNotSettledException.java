package owewell.application;

import owewell.domain.GroupId;

// Thrown when someone tries to delete a group while people still owe money
public class GroupNotSettledException extends RuntimeException {
    public GroupNotSettledException(GroupId id) {
        super("Settle up before deleting this group");
    }
}
