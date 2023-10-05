package com.example.goldencrow.file;


import com.example.goldencrow.file.FileEntity;
import org.bson.types.ObjectId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<FileEntity, ObjectId> {

    /**
     * Sequence로 단일 팡리 조회
     * @param id must not be {@literal null}.
     * @return 파일 Entity
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    Optional<FileEntity> findById(ObjectId id);

    /**
     * 팀 Sequence와 파일 경로로 파일(디렉토리) 조회
     * @param teamSeq 조회하려는 팀 Sequence
     * @param filePath 조회하려는 파일 경로
     * @return 파일 Entity
     */
    Optional<FileEntity> findFileEntityByTeamSeqAndFilePath(Long teamSeq, String filePath);

    /**
     * 팀 Sequence로 파일 조회
     * @param teamSeq 조회하려는 팀 Sequence
     * @return 파일 List
     */
    Optional<List<FileEntity>> findAllByTeamSeq(Long teamSeq);



}