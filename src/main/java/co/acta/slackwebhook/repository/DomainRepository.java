package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface DomainRepository extends JpaRepository<DomainEntity, Long> {
    Optional<DomainEntity> findByDomain(String domain);
}
