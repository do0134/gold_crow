package com.example.goldencrow.liveCode.repository;

import com.example.goldencrow.liveCode.entity.LiveFileEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface LiveFileRepository extends MongoRepository<LiveFileEntity, ObjectId> {
    Optional<LiveFileEntity> findByPath(String path);
}
