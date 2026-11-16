package owewell.api;

import java.util.List;

public record CreateGroupRequest(String name, List<String> members) {}
