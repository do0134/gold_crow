package com.example.goldencrow.unLogin;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnLoginRepository  extends MongoRepository<UnLoginEntity, ObjectId> {
    Optional<UnLoginEntity> findUnLoginEntityBySessionId(String sessionId);
}
