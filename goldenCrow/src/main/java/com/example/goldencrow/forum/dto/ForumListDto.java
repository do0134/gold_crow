package com.example.goldencrow.forum.dto;

import com.example.goldencrow.forum.ForumEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ForumListDto {

    private Long postSeq;
    private String postTitle;
    private String userId;
    private Date postCreatedAt;

    public ForumListDto(ForumEntity forumEntity) {
        this.postSeq = forumEntity.getPostSeq();
        this.postTitle = forumEntity.getPostTitle();
        this.userId = forumEntity.getUser().getUserId();
        this.postCreatedAt = forumEntity.getPostCreatedAt();
    }
}
