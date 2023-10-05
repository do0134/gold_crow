package com.example.goldencrow.file.dto;

import lombok.Data;

/**
 * FileCreateRequest DTO
 */
@Data
public class FileCreateRequestDto {
    private String fileTitle;
    private String filePath;
    private Long teamSeq;

    /**
     * 빈 FileCreateRequestDto 생성자
     */
    public FileCreateRequestDto() {
    }

    /**
     * FileCreateRequestDto 생성자
     *
     * @param fileTitle 파일의 이름
     * @param filePath  파일의 경로
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    public FileCreateRequestDto(String fileTitle, String filePath) {
        this.fileTitle = fileTitle;
        this.filePath = filePath;
    }
}
