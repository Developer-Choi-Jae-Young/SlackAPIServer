package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<DomainEntity, Long> {
}
