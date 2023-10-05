package com.example.goldencrow.user.dto;

import lombok.Data;

/**
 * 사용자의 탭 정보를 담는 DTO
 */
@Data
public class SettingTapDto {

    /**
     * 탭이 띄우고 있는 파일의 이름
     */
    private String name;

    /**
     * 탭이 띄우고 있는 파일의 경로
     */
    private String path;

    /**
     * 가장 마지막에 활성화 되어있던 탭인지의 여부
     * OPTION. "Y", "N"
     * DEFAULT. "N"
     */
    private String isLast;

    /**
     * 빈 SettingTabDto 생성자
     */
    public SettingTapDto() {
        this.isLast = "N";
    }

}
