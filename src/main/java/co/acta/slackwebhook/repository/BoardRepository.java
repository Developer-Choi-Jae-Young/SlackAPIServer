package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
}
