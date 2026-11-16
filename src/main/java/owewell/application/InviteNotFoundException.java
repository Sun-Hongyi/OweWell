package owewell.application;

public class InviteNotFoundException extends RuntimeException {
    public InviteNotFoundException() {
        super("This invite link is not valid");
    }
}
