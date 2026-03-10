package co.acta.slackwebhook.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BoardEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String writer;
    private LocalDate regDate;
    private Long boardId;
    private String ts;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domainChannelId")
    private DomainChannelEntity domainChannel;
}
