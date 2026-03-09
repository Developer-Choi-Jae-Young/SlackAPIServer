package co.acta.slackwebhook.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DomainEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String domain;
    private String replyUrl;
    private String viewUrl;
}
