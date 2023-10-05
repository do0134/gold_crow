package com.example.goldencrow.user.dto;

import lombok.Data;

import java.util.List;

/**
 * 사용자별 개인 환경 세팅의 입출력으로 사용될 DTO
 */
@Data
public class SettingsDto {

    /**
     * 개인 환경 세팅 조회 시 정상적으로 조회되었는지를 확인하는 단서
     * OPTION. (Constants 참조)
     */
    private String result;

    /**
     * 에디터와 콘솔의 비율
     */
    private int horizonSplit;

    /**
     * 사용자가 열어둔 탭의 리스트
     */
    private List<SettingTapDto> lastTab;

    /**
     * 사용자가 마지막에 연 사이드 바 메뉴
     */
    private String lastSideBar;

    /**
     * 사용자의 에디터 설정
     */
    private SettingEditorDto editors;

    /**
     * 사용자의 콘솔 설정
     */
    private SettingConsoleDto consoles;

    /**
     * 빈 SettingsDto 생성자
     */
    public SettingsDto() {
    }

}
