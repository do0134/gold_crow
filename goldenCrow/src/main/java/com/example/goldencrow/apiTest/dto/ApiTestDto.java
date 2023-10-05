package com.example.goldencrow.apiTest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

/**
 * API 테스트 기능에 입력으로 사용될 DTO
 */
@Data
@NoArgsConstructor
public class ApiTestDto {

    /**
     * 테스트 하고자 하는 api 주소
     */
    private String api;

    /**
     * api 요청의 method
     * OPTION. "GET", "POST", "PUT", "DELETE"
     */
    private String type;

    /**
     * 요청에 담아 보낼 request body
     */
    private JSONObject request;

    /**
     * 요청의 header 정보
     * EX. Authorization, Content-Type...
     */
    private JSONObject header;

}
