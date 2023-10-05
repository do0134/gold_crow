package com.example.goldencrow.team.repository;

import com.example.goldencrow.team.entity.TeamEntity;
import com.example.goldencrow.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Team Table에 접속하는 Repository
 */
@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    /**
     * 입력받은 UserSeq와 TeamName을 가지는 Team을 찾는 쿼리
     *
     * @param userSeq 팀장의 UserSeq
     * @param teamName 팀의 이름
     * @return Optional<TeamEntity>를 반환
     */
    Optional<TeamEntity> findTeamEntityByTeamLeader_UserSeqAndTeamName(Long userSeq, String teamName);

    /**
     * 입력받은 TeamSeq와 UserSeq를 가지는 Team을 찾는 쿼리
     *
     * @param teamSeq 팀의 Seq
     * @param userSeq 팀장의 UserSeq
     * @return Optional<TeamEntity>를 반환
     */
    Optional<TeamEntity> findByTeamSeqAndTeamLeader_UserSeq(Long teamSeq, Long userSeq);

    /**
     * 입력받은 UserSeq를 가지는 Team의 리스트를 찾는 쿼리
     *
     * @param userSeq 팀장의 UserSeq
     * @return List<TeamEntity>를 반환
     */
    List<TeamEntity> findAllByTeamLeader_UserSeq(Long userSeq);

    /**
     * 입력받은 TeamSeq를 가지는 Team을 찾는 쿼리
     *
     * @param teamSeq 팀의 Seq
     * @return Optional<TeamEntity>를 반환
     */
    Optional<TeamEntity> findByTeamSeq(Long teamSeq);

    Optional<TeamEntity> findTeamPortByTeamSeq(Long teamSeq);
}
