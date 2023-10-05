package com.example.goldencrow.unLogin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document(collection = "unlogin")
@Getter @Setter
public class UnLoginEntity {
    @Id
    private ObjectId id;

    private String sessionId;

    @Builder
    public UnLoginEntity(String sessionId) {
        this.sessionId = sessionId;
    }
}
