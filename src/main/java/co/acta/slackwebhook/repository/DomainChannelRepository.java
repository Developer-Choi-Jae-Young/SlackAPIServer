package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.DomainChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DomainChannelRepository extends JpaRepository<DomainChannelEntity, Long> {
    List<DomainChannelEntity> findByDomain_Domain(String domain);
    Optional<DomainChannelEntity> findByChannel(String channel);
}
