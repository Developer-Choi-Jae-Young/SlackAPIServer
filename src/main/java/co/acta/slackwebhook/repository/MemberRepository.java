package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUserTokenAndDelYn(String userToken, boolean delYn);
}
