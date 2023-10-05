package com.example.goldencrow.liveCode.service;

import com.example.goldencrow.liveCode.dto.FileContentSaveDto;
import com.example.goldencrow.liveCode.entity.LiveFileEntity;
import com.example.goldencrow.liveCode.repository.LiveFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LiveFileService {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    public LiveFileRepository liveFileRepository;

    /**
     * 받은 content를 저장하는 함수
     * @param body FileContentSaveDto
     * @return fileContentSaveDto
     */
    public FileContentSaveDto insertLive(FileContentSaveDto body) {

        Optional<LiveFileEntity> lfe = liveFileRepository.findByPath(body.getPath());

        LiveFileEntity liveFileEntity = validate(lfe,body);

        FileContentSaveDto fileContentSaveDto = new FileContentSaveDto(liveFileEntity.getContent(), liveFileEntity.getPath());

        return fileContentSaveDto;
    }

    /**
     * Optional Entity가 존재하는지 확인하는 함수
     * @param lfe
     * @param body
     * @return liveFileEntity
     */
    public LiveFileEntity validate(Optional<LiveFileEntity> lfe, FileContentSaveDto body) {
        LiveFileEntity liveFileEntity;

        if (!lfe.isPresent()) {
            liveFileEntity = new LiveFileEntity(body.getContent(), body.getPath());
            mongoTemplate.insert(liveFileEntity);
        } else {
            liveFileEntity = lfe.get();
            liveFileEntity.setContent(body.getContent());
            liveFileRepository.save(liveFileEntity);
        }

        return liveFileEntity;
    }
}
