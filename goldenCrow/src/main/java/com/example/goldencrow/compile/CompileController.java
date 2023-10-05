package com.example.goldencrow.compile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * 파일 혹은 프로젝트의 컴파일을 처리하는 Controller
 *
 * @url /api/compile
 */
@RestController
@RequestMapping(value = "/api/compile")
public class CompileController {
    private final CompileService compileService;

    /**
     * CompileController 생성자
     *
     * @param compileService 컴파일 관련 로직을 처리하는 Service
     */
    public CompileController(CompileService compileService) {
        this.compileService = compileService;
    }

    /**
     * 컴파일 API
     * access token 필요
     *
     * @param req "filePath" ,"input"을 key로 가지는 Map<String, String>
     * @return 컴파일 성공 시 컴파일 결과 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PostMapping("/py")
    public ResponseEntity<Map<String, String>> pyCompilePost(@RequestBody Map<String, String> req) {

        if (req.containsKey("filePath") && req.containsKey("input")) {
            String filePath = req.get("filePath");
            String input = req.get("input");
            System.out.println("여기는 compilercontroller 들어오는 filePath : " +filePath);
            Map<String, String> typeRes = compileService.findProjectTypeService(filePath);
            int type = Integer.parseInt(typeRes.get("type"));
            String mainPath = typeRes.get("path");
            System.out.println(type);
            if (type == 0) {
                System.out.println("파일이 없어서 null이야 그래서 0이라서 BAD");
                Map<String, String> res = new HashMap<>();
                res.put("result", BAD_REQ);
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
            Map<String, String> res = compileService.pyCompileService(type, mainPath, input);
            String result = res.get("result");
            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_SUCH:
                    return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
                case DUPLICATE:
                    return new ResponseEntity<>(res, HttpStatus.CONFLICT);
                default:
                    return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
        } else {
            Map<String, String> res = new HashMap<>();
            res.put("result", BAD_REQ);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 컴파일 중지 API
     * access token 필요
     *
     * @param req "teamName", "teamSeq"을 key로 가지는 Map<String, String>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PostMapping("/py/stop")
    public ResponseEntity<Map<String, String>> pyCompileStopPost(@RequestBody Map<String, String> req) {

        if (req.containsKey("teamName") && req.containsKey("teamSeq")) {
            String teamName = req.get("teamName");
            String teamSeq = req.get("teamSeq");
            Map<String, String> res = compileService.pyCompileStopService(teamName, teamSeq);
            String result = res.get("result");
            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_SUCH:
                    return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
                default:
                    return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
            }
        } else {
            Map<String, String> res = new HashMap<>();
            res.put("result", BAD_REQ);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }


}
