package com.example.goldencrow.apiTest;

import com.example.goldencrow.apiTest.dto.ApiTestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * api 응답테스트와 관련된 로직을 처리하는 Service
 */
@Service
public class ApiTestService {

    /**
     * RestTemplate을 사용하기 위한 로직
     *
     * @return RestTemplateBuilder
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    public RestTemplate restTemplate = restTemplate();

    /**
     * api 응답테스트를 수행하는 내부 로직
     * ApiTestDto의 변수들을 이용해, RestTemaplate 통해 Api 통신을 하고 결과를 반환
     *
     * @param apiTestDto "api", "type", "request", "header" key를 가진 Dto
     * @return api 응답 성공 시 응답 결과와 응답 시간 반환, 성패에 따른 result 반환
     */
    public Map<String, Object> apiTestService(ApiTestDto apiTestDto) {
        Map<String, Object> serviceRes = new HashMap<>();

        String api = apiTestDto.getApi();
        String type = apiTestDto.getType();
        JSONObject objRequest = apiTestDto.getRequest();
        JSONObject objHeaders = apiTestDto.getHeader();

        // 객체 convert를 위해 ObjectMapper 사용
        ObjectMapper objectMapper = new ObjectMapper();
        @SuppressWarnings("unchecked") Map<String, String> mapHeader = objectMapper.convertValue(objHeaders, Map.class);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.setAll(mapHeader);

        // 해당 API에 요청할 request 생성
        HttpEntity<String> request = new HttpEntity<>(objRequest.toJSONString(), headers);

        // Request Type에 따라 API 요청
        try {
            Object response;
            long beforeTime = System.currentTimeMillis();
            switch (type.toUpperCase()) {
                case "GET":
                    response = restTemplate.exchange(api, HttpMethod.GET, request, Object.class);
                    break;
                case "POST":
                    response = restTemplate.exchange(api, HttpMethod.POST, request, Object.class);
                    break;
                case "PUT":
                    response = restTemplate.exchange(api, HttpMethod.PUT, request, Object.class);
                    break;
                default:
                    response = restTemplate.exchange(api, HttpMethod.DELETE, request, Object.class);
                    break;
            }
            long afterTime = System.currentTimeMillis();
            // 응답시간 계산
            long diffTime = afterTime - beforeTime;

            @SuppressWarnings("unchecked") Map<String, Object> body
                    = new ObjectMapper().convertValue(response, Map.class);

            serviceRes.put("data", body.get("body"));
            serviceRes.put("time", diffTime);
            serviceRes.put("result", SUCCESS);
        } catch (Exception e) {
            serviceRes.put("result", WRONG);
        }
        return serviceRes;
    }
}
