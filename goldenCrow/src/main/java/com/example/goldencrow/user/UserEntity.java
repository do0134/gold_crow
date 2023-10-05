package com.example.goldencrow.user;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * User Table Entity
 */
@Entity
@DynamicUpdate
@Table(name = "user")
@Data
public class UserEntity {

    /**
     * Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long userSeq;

    @Column
    private String userId;

    @Column
    private String userPassWord;

    @Column
    private String userNickname;

    @Column
    private String userProfile;

    @Column
    private String userGitUsername;

    @Column
    private String userGitToken;

    /**
     * 빈 UserEntity 생성자
     */
    public UserEntity() {
    }

    /**
     * UserEntity 생성자
     *
     * @param userId 사용자의 아이디
     * @param userNickname 사용자의 닉네임
     */
    public UserEntity(String userId, String userNickname) {
        this.userId = userId;
        this.userNickname = userNickname;

        // userPassword는 service 단에서 encoding을 거쳐 기록

    }
}
