package co.acta.slackwebhook.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DomainEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
