package com.example.goldencrow.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Table에 접속하는 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 입력받은 UserId를 가지는 User를 찾는 쿼리
     *
     * @param userId 사용자의 아이디
     * @return Optional<UserEntity>를 반환
     */
    Optional<UserEntity> findUserEntityByUserId(String userId);

    /**
     * 입력받은 검색어를 UserId 혹은 UserNickname에 가지는 User를 찾는 쿼리
     *
     * @param word1 검색어1
     * @param word2 검색어2 (검색어1과 같이 쓰임)
     * @return List<UserEntity>를 반환
     */
    List<UserEntity> findAllByUserIdContainingOrUserNicknameContaining(String word1, String word2);

    /**
     * 입력받은 UserSeq를 가지는 User를 찾는 쿼리
     *
     * @param userSeq 사용자의 UserSeq
     * @return Optional<UserEntity>를 반환
     */
    Optional<UserEntity> findByUserSeq(Long userSeq);

}
