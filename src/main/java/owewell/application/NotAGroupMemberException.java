package owewell.application;

import owewell.domain.GroupId;
import owewell.domain.UserId;

public class NotAGroupMemberException extends RuntimeException {
    public NotAGroupMemberException(GroupId groupId, UserId user) {
        super("User " + user.value() + " is not a member of group " + groupId.value());
    }
}
