package com.example.goldencrow.team.entity;

import com.example.goldencrow.user.UserEntity;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * Team Table Entity
 */
@Entity
@DynamicUpdate
@Table(name = "team")
@Data
public class TeamEntity {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long teamSeq;

    @Column
    private String teamName;

    @ManyToOne
    @JoinColumn(name = "teamLeaderSeq", referencedColumnName = "userSeq")
    private UserEntity teamLeader;

    @Column
    private String teamGit;

    @Column
    private String teamPort;

    @Column
    private int projectType;

    /**
     * 빈 TeamEntity 생성자
     */
    public TeamEntity() {
    }

    /**
     * TeamEntity 생성자
     *
     * @param userEntity 사용자의 UserEntity
     * @param teamName    만들고자 하는 팀의 이름
     */
    public TeamEntity(UserEntity userEntity, String teamName, String teamGit, int projectType) {
        this.teamLeader = userEntity;
        this.teamName = teamName;
        this.teamGit = teamGit;
        this.teamPort = String.valueOf(0);
        this.projectType = projectType;

        // teamPort는 도커 파일 생성 이후 등록
        // teamGit은 프로젝트 생성 시점에 등록

    }
}
