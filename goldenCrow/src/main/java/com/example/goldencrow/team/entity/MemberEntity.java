package com.example.goldencrow.team.entity;

import com.example.goldencrow.user.UserEntity;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * Member Table Entity
 */
@Entity
@DynamicUpdate
@Table(name = "member")
@Data
public class MemberEntity {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long memberSeq;

    @ManyToOne
    @JoinColumn(name = "userSeq", referencedColumnName = "userSeq")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "teamSeq", referencedColumnName = "teamSeq")
    private TeamEntity team;

    @Column
    private String settings;

    /**
     * 빈 MemberEntity 생성자
     */
    public MemberEntity() {
    }

    /**
     * MemberEntity 생성자
     *
     * @param userEntity 사용자의 UserEntity
     * @param teamEntity 팀의 TeamEntity
     */
    public MemberEntity(UserEntity userEntity, TeamEntity teamEntity) {
        this.user = userEntity;
        this.team = teamEntity;
        this.settings = "";
    }
}
