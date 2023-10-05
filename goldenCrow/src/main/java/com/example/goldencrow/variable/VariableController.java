package com.example.goldencrow.variable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * 변수명을 추천하는 Controller
 *
 * @url /api/variable
 */

@RestController
@RequestMapping("/api/variable")
public class VariableController {

    public VariableService variableService;

    /**
     * VariableController 생성자
     *
     * @param variableService 변수명 관련 로직을 처리하는 Service
     */
    public VariableController(VariableService variableService) {
        this.variableService = variableService;
    }

    /**
     * 변수명 추천 API
     * access token 필요
     *
     * @param req "data"를 key로 가지는 Map<String, String>
     * @return 추천하는 변수명 리스트 (camelCase, PascalCase, snake_case) 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> variableRecommendPost(@RequestBody Map<String, String> req) {
        if (req.containsKey("data")) {
            String data = req.get("data");
            Map<String, Object> res = variableService.variableRecommendService(data);
            String result = (String) res.get("result");
            if (result.equals(SUCCESS)) {
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            }
        } else {
            Map<String, Object> res = new HashMap<>();
            res.put("result", BAD_REQ);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }
}
