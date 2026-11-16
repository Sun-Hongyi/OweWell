package owewell;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import owewell.api.AppOidcUserService;
import owewell.application.GroupRepository;
import owewell.application.GroupService;
import owewell.application.UserRepository;
import owewell.domain.AutoSettlement;
import owewell.domain.SettlementAlgorithm;
import owewell.infrastructure.PostgresGroupRepository;
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
}
