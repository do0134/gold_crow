package com.example.goldencrow.liveCode.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "liveFile")
@Getter @Setter
public class LiveFileEntity {

    @Id
    private ObjectId id;

    @Lob
    private String content;

    private String path;


    @Builder
    public LiveFileEntity(String content, String path) {
        this.content = content;
        this.path = path;
    }

}
