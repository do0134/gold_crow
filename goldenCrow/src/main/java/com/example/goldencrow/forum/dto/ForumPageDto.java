package com.example.goldencrow.forum.dto;

import com.example.goldencrow.forum.ForumEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ForumPageDto {

    //    private int totalPage;
    //    private int page;
    private List<ForumListDto> forumList;

    private ForumDto.ForumResult result;

    public ForumPageDto() {
    }

    public ForumPageDto(List<ForumEntity> forumEntity) {
        this.forumList = new ArrayList<>();
        for (ForumEntity forum : forumEntity) {
            forumList.add(new ForumListDto(forum));
        }
    }
}
