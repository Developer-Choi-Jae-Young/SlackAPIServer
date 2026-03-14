package co.acta.slackwebhook.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** 부모 게시글 Slack ts */
    private String ts;

    /** Slack 답글 자체의 ts (thread reply ts) — 수정/삭제 식별자 */
    @Column(unique = true)
    private String replyTs;

    /** BO 시스템 댓글 PK (등록 응답에서 파싱, null일 수 있음) */
    private String boReplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardId")
    private BoardEntity board;

    public void updateTs(String newTs) {
        this.ts = newTs;
    }
}
