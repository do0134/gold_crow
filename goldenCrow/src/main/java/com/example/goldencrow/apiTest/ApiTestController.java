package com.example.goldencrow.apiTest;

import com.example.goldencrow.apiTest.dto.ApiTestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.example.goldencrow.common.Constants.SUCCESS;

/**
 * 사용자의 api의 응답테스트를 처리하는 Controller
 *
 * @url /api/api-test
 */
@RestController
@RequestMapping(value = "/api/api-test")
public class ApiTestController {
    private final ApiTestService apiTestService;

    /**
     * ApiTestController의 생성자
     *
     * @param apiTestService api 응답테스트와 관련된 로직을 처리하는 Service
     */
    public ApiTestController(ApiTestService apiTestService) {
        this.apiTestService = apiTestService;
    }

    /**
     * API 응답 테스트 API
     * access token 필요
     * <p>
     * ApiTestDto의 변수들을 이용해 API 테스트를 진행
     * [EN] Opearting API Communication with variables of ApiTestDto
     *
     * @param apiTestDto api: URI, type: HTTP Method, request: RequestBody, header: Headers
     * @return 성공 시 응답시간과 응답 결과를 반환, 성패에 따른 result 반환
     * @status 200, 400, 401
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> apiTestPost(@RequestBody ApiTestDto apiTestDto) {
        Map<String, Object> res = apiTestService.apiTestService(apiTestDto);
        String result = (String) res.get("result");
        if (result.equals(SUCCESS)) {
            return new ResponseEntity<>(res, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }
}
