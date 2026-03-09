package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    Optional<BoardEntity> findByBoardId(Long boardId);
}
