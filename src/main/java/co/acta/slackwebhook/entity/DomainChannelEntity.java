package co.acta.slackwebhook.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(
        name="DOMAIN_CHANNEL",
        uniqueConstraints={
                @UniqueConstraint(name= "unique-constraint-domain-channel", columnNames={"domain", "channel"})
        }
)
public class DomainChannelEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String domain;
    private String channel;
}
