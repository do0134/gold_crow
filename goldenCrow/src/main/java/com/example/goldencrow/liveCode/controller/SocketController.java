package com.example.goldencrow.liveCode.controller;

import com.example.goldencrow.liveCode.dto.FileContentSaveDto;
import com.example.goldencrow.liveCode.service.LiveFileService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SocketController {
    private final LiveFileService liveFileService;

    /**
     * 내용을 입력받은 다음, DB에 있는 content를 업데이트
     * @param body FileContentSaveDto
     * @return
     * @throws Exception
     */
    @MessageMapping("/share/{teamSeq}")
    @SendTo("/topic/{teamSeq}")
    public JSONObject saveContent(@DestinationVariable Long teamSeq, Map<String,String> body) throws Exception {
        String user = body.get("user");
        String filePath = body.get("filePath");
        Map<String, String> resMap = new HashMap<>();
        resMap.put("user",user);
        resMap.put("filePath", filePath);
        JSONObject res = new JSONObject(resMap);
        return res;
    }
}
