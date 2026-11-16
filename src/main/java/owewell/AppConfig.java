package owewell;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import owewell.application.GroupRepository;
import owewell.application.GroupService;
import owewell.domain.AutoSettlement;
import owewell.domain.SettlementAlgorithm;
import owewell.infrastructure.InMemoryGroupRepository;

@Configuration
public class AppConfig {

    @Bean
    public GroupRepository groupRepository() {
        return new InMemoryGroupRepository();
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
}
