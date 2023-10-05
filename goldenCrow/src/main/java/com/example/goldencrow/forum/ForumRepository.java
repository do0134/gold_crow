package com.example.goldencrow.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumRepository extends JpaRepository<ForumEntity, Long> {

    // 포럼 게시글 총 갯수
    int countAllBy();

    // 게시글 전체 목록 조회
//    @Query(value="select * from forum ORDER BY postSeq DESC OFFSET :ForumOffset ROWS FETCH NEXT :forumL ROWS ONLY", nativeQuery = true)
//    List<ForumEntity> findRecentList(@Param(value="limit") int forumL, @Param(value="offset") int ForumOffset);


    // 유저가 작성한 게시글 조회
    List<ForumEntity> findForumEntitiesByUser(Long seq);

    // 단일 작품 조회
    ForumEntity findByPostSeq(Long postSeq);

    // 검색한 내용 조회
    List<ForumEntity> findForumEntitiesByPostTitleContainingOrderByPostSeqDesc(String text);

}
