package com.example.goldencrow.team.dto;

import com.example.goldencrow.team.entity.MemberEntity;
import com.example.goldencrow.user.dto.UserInfoDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 팀원 목록 조회의 내부 로직에 사용되는 DTO
 */
@Data
public class UserInfoListDto {

    private String result;

    private List<UserInfoDto> userInfoDtoList;

    /**
     * 빈 UserInfoDto 생성자
     */
    public UserInfoListDto() {
    }

    /**
     * UserInfoListDto 생성자
     *
     * @param memberEntityList MemberEntity의 리스트
     */
    public UserInfoListDto(List<MemberEntity> memberEntityList) {

        List<UserInfoDto> userInfoDtoList = new ArrayList<>();
        for (MemberEntity m : memberEntityList) {
            userInfoDtoList.add(new UserInfoDto(m.getUser()));
        }

        this.userInfoDtoList = userInfoDtoList;

    }

}
