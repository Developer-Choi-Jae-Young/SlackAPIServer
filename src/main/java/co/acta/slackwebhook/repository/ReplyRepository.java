package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.ReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReplyRepository extends JpaRepository<ReplyEntity, Long> {
    Optional<ReplyEntity> findByReplyTs(String replyTs);
    Optional<ReplyEntity> findByBoReplyId(String boReplyId);
}
