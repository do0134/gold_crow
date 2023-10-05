package com.example.goldencrow.file;

import com.example.goldencrow.file.dto.FileCreateDto;

import com.sun.istack.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.util.Date;

/**
 * file Entity
 */
@Document(collection = "file")
@Getter
@Setter
public class FileEntity {

    /**
     * Primary Key
     */
    @Id
    @NotNull
    private ObjectId id;
    @NotNull
    private Long teamSeq;
    @NotNull
    private String fileTitle;
    @NotNull
    private String filePath;


    @CreatedDate
    @Column(updatable = false)
    private Date fileCreatedAt;

    @LastModifiedDate
    private Date fileUpdatedAt;

    /**
     * 빈 FileEntity 생성자
     */
    public FileEntity() {
    }

    /**
     * FileEntity 생성자
     *
     * @param fileCreateDto 파일 정보가 저장되어있는 Dto
     */
    public FileEntity(FileCreateDto fileCreateDto) {
        this.fileTitle = fileCreateDto.getFileTitle();
        this.filePath = fileCreateDto.getFilePath();
        this.teamSeq = fileCreateDto.getTeamSeq();
    }

}
