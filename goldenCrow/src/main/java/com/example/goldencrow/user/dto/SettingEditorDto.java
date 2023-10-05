package com.example.goldencrow.user.dto;

import lombok.Data;

/**
 * 사용자의 에디터 정보를 담는 DTO
 */
@Data
public class SettingEditorDto {

    /**
     * 에디터에 표시될 글자 크기
     */
    private int fontSize;

    /**
     * 에디터에 적용될 글꼴
     */
    private String font;

    /**
     * 에디터의 자동 줄바꿈 여부
     * OPTION. "on", "off"
     * DEFAULT. "on"
     */
    private String autoLine;

    /**
     * 빈 SettingEditorDto 생성자
     */
    public SettingEditorDto() {
        this.autoLine = "on";
    }

}
