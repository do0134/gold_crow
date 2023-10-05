package com.example.goldencrow.file.dto;

import lombok.Data;

/**
 * 파일 생성 DTO
 */
@Data
public class FileCreateDto {
    private String fileTitle;
    private String filePath;
    private Long teamSeq;

    /**
     * 빈 FileCreateDto 생성자
     */
    public FileCreateDto() {
    }

    /**
     * FileCreateDto 생성자
     *
     * @param fileTitle 파일 이름
     * @param filePath  파일 경로
     * @param teamSeq   파일의 프로젝트의 팀 Sequence
     */
    public FileCreateDto(String fileTitle, String filePath, Long teamSeq) {
        this.fileTitle = fileTitle;
        this.filePath = filePath;
        this.teamSeq = teamSeq;
    }
}
