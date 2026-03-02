package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.DomainChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomainChannelRepository extends JpaRepository<DomainChannelEntity, Long> {
    List<DomainChannelEntity> findByDomain(String domain);
}
