package com.example.goldencrow.forum.dto;

import com.example.goldencrow.forum.ForumEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ForumDto {

    private Long postSeq;
    private String postTitle;
    private String userId;
    private String postContent;
    private Date postCreatedAt;
    private Date postUpdatedAt;
    private ForumResult result;

    public ForumDto(ForumEntity forum) {
        this.postSeq = forum.getPostSeq();
        this.postTitle = forum.getPostTitle();
        this.postContent = forum.getPostContent();
        this.postCreatedAt = forum.getPostCreatedAt();
        this.postUpdatedAt = forum.getPostUpdatedAt();
        this.userId = forum.getUser().getUserId();
    }

    public enum ForumResult {
        SUCCESS, FAILURE, NO_SUCH_PIECE,
    }

}
