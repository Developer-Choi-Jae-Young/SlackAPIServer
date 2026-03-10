package co.acta.slackwebhook.repository;

import co.acta.slackwebhook.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    Optional<BoardEntity> findByBoardId(Long boardId);

    Optional<BoardEntity> findByTs(String ts);

    Optional<BoardEntity> findByTsAndDomainChannel_Channel(String ts, String domainChannelChannel);
}
