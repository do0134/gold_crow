package com.example.goldencrow.team.repository;

import com.example.goldencrow.team.entity.MemberEntity;
import com.example.goldencrow.team.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Memeber Entity에 접속하는 Repository
 */
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    /**
     * 입력받은 UserSeq를 가지는 Member 리스트를 찾는 쿼리
     *
     * @param userSeq 사용자의 UserSeq
     * @return List<MemberEntity>를 반환함
     */
    List<MemberEntity> findAllByUser_UserSeq(Long userSeq);

    /**
     * 입력받은 UserSeq와 TeamSeq를 가지는 Member를 찾는 쿼리
     *
     * @param userSeq 사용자의 UserSeq
     * @param teamSeq 팀의 Seq
     * @return Optional<MemberEntity>를 반환함
     */
    Optional<MemberEntity> findByUser_UserSeqAndTeam_TeamSeq(Long userSeq, Long teamSeq);

    /**
     * 입력받은 TeamSeq를 가지는 Member 리스트를 찾는 쿼리
     *
     * @param teamSeq 팀의 Seq
     * @return List<MemberEntity>를 반환함
     */
    List<MemberEntity> findAllByTeam_TeamSeq(Long teamSeq);

    /**
     * 입력받은 TeamSeq를 가지는 Member의 수를 세는 쿼리
     *
     * @param teamSeq 팀의 Seq
     * @return 입력받은 TeamSeq를 가지는 Member 명수를 반환
     */
    int countAllByTeam_TeamSeq(Long teamSeq);

}
