package owewell.application;

public class InviteExpiredException extends RuntimeException {
    public InviteExpiredException() {
        super("This invite link has expired. Ask a group member for a new one.");
    }
}
