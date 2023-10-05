package com.example.goldencrow.forum;

import com.example.goldencrow.forum.dto.ForumDto;
import com.example.goldencrow.user.UserEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@Data
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "forum")
public class ForumEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long postSeq;

    @ManyToOne
    @JoinColumn(name = "userSeq", referencedColumnName = "userSeq")
    private UserEntity user;

    @Column(nullable = false)
    private String postTitle;

    @Column(nullable = false)
    private String postContent;

    @CreatedDate
    @Column
    private Date postCreatedAt;

    @LastModifiedDate
    @Column
    private Date postUpdatedAt;

    public ForumEntity(ForumDto forumDto) {
        this.postTitle = forumDto.getPostTitle();
        this.postContent = forumDto.getPostContent();
        this.postCreatedAt = forumDto.getPostCreatedAt();
        this.postUpdatedAt = forumDto.getPostUpdatedAt();
    }
}
