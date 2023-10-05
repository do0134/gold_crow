package com.example.goldencrow.team.dto;

import com.example.goldencrow.team.entity.MemberEntity;
import com.example.goldencrow.user.UserEntity;
import lombok.Data;

/**
 * TeamDto에서 멤버 정보를 표시하는 DTO
 */
@Data
public class MemberDto {

    private Long memberSeq;

    private String memberNickname;

    private String memberProfile;

    /**
     * 빈 MemberDto
     */
    public MemberDto() {
    }

    /**
     * MemberDto 생성자
     *
     * @param memberEntity 그 팀의 MemberEntity
     */
    public MemberDto(MemberEntity memberEntity) {
        UserEntity userEntity = memberEntity.getUser();
        this.memberSeq = userEntity.getUserSeq();
        this.memberNickname = userEntity.getUserNickname();
        this.memberProfile = userEntity.getUserProfile();
    }

}
