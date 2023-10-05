package com.example.goldencrow.forum;

import com.example.goldencrow.forum.dto.ForumDto;
import com.example.goldencrow.forum.dto.ForumPageDto;
import com.example.goldencrow.user.UserEntity;
import com.example.goldencrow.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumService {

    //    public int LIMIT = 8;
    @Autowired
    private ForumRepository forumRepository;

    @Autowired
    private UserRepository userRepository;

    public String forumCheck(Long userSeq, Long forumSeq) {
        if (forumRepository.findById(forumSeq).isPresent()) {
            Long writeSeq = forumRepository.findById(forumSeq).get().getUser().getUserSeq();
            if (userSeq.equals(writeSeq)) {
                return "success";
            } else {
                return "fail";
            }
        } else {
            return "fail";
        }
    }

    public String forumUpload(ForumDto forumDto, Long userSeq) {
        ForumEntity forumEntity = new ForumEntity(forumDto);
        UserEntity userEntity = userRepository.findByUserSeq(userSeq).get();
        forumEntity.setUser(userEntity);

        try {
            forumRepository.saveAndFlush(forumEntity);
        } catch (Exception e) {
            return "fail";
        }
        return "success";
    }

    // 작품 목록 최신순 조회
    public ForumPageDto forumAll() {

        List<ForumEntity> forumEntityList;

        forumEntityList = forumRepository.findAll();

        return new ForumPageDto(forumEntityList);
    }

    // 게시글 하나 읽기
    public ForumDto forumOne(Long forumSeq) {
        ForumEntity forumEntity = forumRepository.findByPostSeq(forumSeq);

        if (forumEntity == null) {
            ForumDto forumDto = new ForumDto();
            forumDto.setResult(ForumDto.ForumResult.FAILURE);
            return forumDto;
        } else {
            ForumDto forumDto = new ForumDto(forumEntity);
            forumDto.setResult(ForumDto.ForumResult.SUCCESS);
            return forumDto;
        }
    }

    public String forumUpdate(ForumDto forumDto, Long forumSeq) {

        try {
            ForumEntity forumEntity = forumRepository.findByPostSeq(forumSeq);
            forumEntity.setPostTitle(forumDto.getPostTitle());
            forumEntity.setPostContent(forumDto.getPostContent());
            forumRepository.saveAndFlush(forumEntity);
        } catch (Exception e) {
            return "fail";
        }
        return "success";
    }

    public String forumDelete(Long forumSeq) {
        try {
            forumRepository.deleteById(forumSeq);
        } catch (Exception e) {
            return "fail";
        }
        return "success";
    }
}
