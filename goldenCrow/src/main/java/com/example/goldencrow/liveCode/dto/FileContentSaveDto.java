package com.example.goldencrow.liveCode.dto;

import lombok.Data;

import javax.persistence.Lob;

@Data
public class FileContentSaveDto {

    @Lob
    private String content;

    private String path;

    public FileContentSaveDto(String content, String path) {
        this.content = content;
        this.path = path;
    }

}
