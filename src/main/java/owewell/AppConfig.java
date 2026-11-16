package owewell;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import owewell.api.AppOidcUserService;
import owewell.api.CurrentUser;
import owewell.api.SecurityCurrentUser;
import owewell.application.GroupRepository;
import owewell.application.GroupService;
import owewell.application.InviteRepository;
import owewell.application.InviteService;
import owewell.application.UserRepository;
import owewell.domain.AutoSettlement;
import owewell.domain.SettlementAlgorithm;
import owewell.infrastructure.PostgresGroupRepository;
import owewell.infrastructure.PostgresInviteRepository;
import owewell.infrastructure.PostgresUserRepository;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class AppConfig {

    @Bean
    public GroupRepository groupRepository(JdbcClient jdbc, JsonMapper json) {
        return new PostgresGroupRepository(jdbc, json);
    }

    @Bean
    public SettlementAlgorithm settlementAlgorithm() {
        return new AutoSettlement();
    }

    @Bean
    public GroupService groupService(GroupRepository groupRepository,
                                     SettlementAlgorithm settlementAlgorithm) {
        return new GroupService(groupRepository, settlementAlgorithm);
    }

    @Bean
    public UserRepository userRepository(JdbcClient jdbc) {
        return new PostgresUserRepository(jdbc);
    }

    @Bean
    public AppOidcUserService appOidcUserService(UserRepository userRepository) {
        return new AppOidcUserService(userRepository);
    }

    @Bean
    public CurrentUser currentUser() {
        return new SecurityCurrentUser();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public InviteRepository inviteRepository(JdbcClient jdbc) {
        return new PostgresInviteRepository(jdbc);
    }

    @Bean
    public InviteService inviteService(GroupRepository groupRepository,
                                       InviteRepository inviteRepository, Clock clock) {
        return new InviteService(groupRepository, inviteRepository, clock);
    }
}
