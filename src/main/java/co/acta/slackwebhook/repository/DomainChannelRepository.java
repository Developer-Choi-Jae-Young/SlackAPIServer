package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.DomainChannelEntity;
import co.acta.slackwebhook.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomainChannelRepository extends JpaRepository<DomainChannelEntity, Long> {
    List<DomainChannelEntity> findByDomainAndChannel(DomainEntity domain, String channel);
    List<DomainChannelEntity> findByDomain_Domain(String domain);
}
