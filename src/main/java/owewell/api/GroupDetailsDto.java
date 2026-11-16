package owewell.api;

import java.util.List;

public record GroupDetailsDto(String id, String name, List<MemberDto> members) {}
