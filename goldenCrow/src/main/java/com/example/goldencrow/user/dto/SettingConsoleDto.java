package com.example.goldencrow.user.dto;

import lombok.Data;

/**
 * 사용자의 콘솔 정보를 담는 DTO
 */
@Data
public class SettingConsoleDto {

    /**
     * 콘솔에 표시될 글자 크기
     */
    private int fontSize;

    /**
     * 콘솔에 적용될 글씨체
     */
    private String font;

    /**
     * 빈 SettingConsoleDto 생성자
     */
    public SettingConsoleDto() {
    }

}
