package com.example.goldencrow.unLogin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

@RestController
@RequestMapping(value = "/api/unlogin")
public class UnLoginController {
    @Autowired
    private UnLoginService unLoginService;

    @GetMapping("")
    public ResponseEntity<String> getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        return new ResponseEntity<>(session.getId(), HttpStatus.OK);
    }

    @PostMapping("/compile")
    public ResponseEntity<Map<String, String>> unloginCompilePost(HttpServletRequest request, @RequestBody Map<String, String> req) {
        String sessionId = request.getSession().getId();
        if (req.containsKey("fileContent") && req.containsKey("input")) {
            String fileContent = req.get("fileContent");
            String input = req.get("input");
            Map<String, String> res = unLoginService.unloginCompileService(sessionId, fileContent, input);
            switch (res.get("result")) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
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
}
