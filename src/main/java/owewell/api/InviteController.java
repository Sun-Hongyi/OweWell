package owewell.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import owewell.application.InviteService;
import owewell.domain.GroupId;

@RestController
public class InviteController {

    private final InviteService invites;
    private final CurrentUser currentUser;

    public InviteController(InviteService invites, CurrentUser currentUser) {
        this.invites = invites;
        this.currentUser = currentUser;
    }

    @PostMapping("/groups/{groupId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateInviteResponse createInvite(@PathVariable String groupId) {
        InviteService.CreatedInvite created = invites.createInvite(new GroupId(groupId), currentUser.id());
        return new CreateInviteResponse(created.token(), created.expiresAt().toString());
    }

    @PostMapping("/invites/{token}/accept")
    public AcceptInviteResponse acceptInvite(@PathVariable String token) {
        GroupId groupId = invites.acceptInvite(token, currentUser.id());
        return new AcceptInviteResponse(groupId.value());
    }
}
