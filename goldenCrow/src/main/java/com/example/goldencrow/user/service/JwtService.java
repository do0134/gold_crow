package com.example.goldencrow.user.service;

import com.example.goldencrow.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * jwt를 관리하는 service
 */
@Service
public class JwtService {

    /**
     * Jwt를 생성하는데 쓰일 secret key
     */
    @Value("${secret.jwt.key}")
    private String SECRET_KEY;

    /**
     * 액세스 토큰에 적용될 만료 시간
     * DEFAULT. 30days
     */
    private final long VALID_TIME = Duration.ofDays(30).toMillis();

    private final UserRepository userRepository;

    /**
     * JwtService 생성자
     *
     * @param userRepository User Table에 접속하는 repository
     */
    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 액세스 토큰 생성 내부 로직
     *
     * @param userSeq 사용자의 아이디
     * @return 생성 성공 시 jwt 반환
     */

    public String createAccess(Long userSeq) {

        // 헤더 설정
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT");
        headers.put("alg", "HS256");

        // 페이로드 설정
        Map<String, Object> payloads = new HashMap<>();
        payloads.put("jti", userSeq);

        // 만료일 설정
        Date ext = new Date();
        ext.setTime(ext.getTime() + VALID_TIME);

        // 토큰 빌드
        return Jwts.builder()
                .setHeader(headers)
                .setClaims(payloads)
                .setExpiration(ext)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    /**
     * 토큰이 현재 사용 가능한지 확인하는 내부 로직
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 성패에 따른 result 반환
     */
    public String verifyJWT(String jwt) {

        try {

            Map<String, Object> claimMap = Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(jwt)
                    .getBody();

            Long userSeq = Long.valueOf(String.valueOf(claimMap.get("jti")));

            // 해당하는 사용자가 존재하는지 확인
            if (!userRepository.findById(userSeq).isPresent()) {
                // 해당하는 사용자가 없음
                return NO_SUCH;
            }

            // 위의 과정을 무사히 통과했으므로
            return SUCCESS;

        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            return NO_PER;

        } catch (Exception e) {
            return UNKNOWN;
        }

    }

    /**
     * 토큰이 가지고 있는 UserSeq를 반환하는 내부 로직
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 검증 성공 시 UserSeq 반환
     */
    public Long JWTtoUserSeq(String jwt) {

        // Jwt parser를 이용하여 토큰이 가지고 있는 정보를 추출
        Map<String, Object> claimMap = Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(jwt)
                .getBody();

        // UserSeq를 Long으로 parse하여 반환
        return Long.valueOf(String.valueOf(claimMap.get("jti")));
    }

}

