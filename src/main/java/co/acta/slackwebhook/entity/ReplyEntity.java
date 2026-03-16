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
    private String ts;
    @Column(unique = true)
    private String replyTs;
    private String boReplyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardId")
    private BoardEntity board;
}
