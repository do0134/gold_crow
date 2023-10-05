package com.example.goldencrow.config;

import com.example.goldencrow.common.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfigurer를 상속받은 WebMvcConfig
 * interceptor와 CORS 관련 내부 로직
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    JwtInterceptor jwtInterceptor;

    /**
     * interceptor를 적용 or 미적용을 처리하는 내부 로직
     * addInterceptor : 적용할 인터셉터
     * addPathPatterns : 인터셉터를 적용할 URI pattern
     * excludePathPatterns : 인터셉터를 적용하지 않을 URI pattern
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/users/signup")       // 회원가입
                .excludePathPatterns("/api/users/login")        // 로그인
                .excludePathPatterns("/api/users/mypage/**")    // 프로필 조회
                .excludePathPatterns("/api/users/search/**")    // 유저 검색
                .excludePathPatterns("/api/forum/read")         // 포럼
                .excludePathPatterns("/api/forum/read/**")     // 포럼
                .excludePathPatterns("/api/editors/format/**")  // 포맷팅
                .excludePathPatterns("/api/editors/format/read/**") // 포맷팅 결과 조회
                .excludePathPatterns("api/editors/lint/**")     // 린트
                .excludePathPatterns("/api/api-test")           // API 테스트
                .excludePathPatterns("/api/compile/py")         // python 컴파일
                .excludePathPatterns("/api/compile/py/stop")    // python 컴파일 중지
                .excludePathPatterns("/api/variable")           // 변수명 추천
                .excludePathPatterns("/api/unlogin")            // 비로그인 사용자 세션 처리
                .excludePathPatterns("/api/unlogin/compile");
    }

    /**
     * CORS(교차 출처 리소스 공유)를 위한 내부 로직
     * addMapping : CORS를 적용할 URL패턴
     * allowedOriginPatterns : 허용할 출처(Origin) pattern
     * allowedHeaders : 허용할 Header
     * exposedHeaders : 반환할 Header
     * allowCredentials : 쿠키를 허용
     * allowedMethods : 허용할 HTTP method
     * maxAge : pre-flight 리퀘스트 캐싱할 시간
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(6000);
    }
}
