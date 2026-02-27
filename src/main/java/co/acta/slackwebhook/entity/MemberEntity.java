package co.acta.slackwebhook.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "MEMBER")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberEntity {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String userToken;
    private String email;
    private String phone;
    @Column(columnDefinition = "boolean default false")
    private boolean delYn;
}
