package com.example.goldencrow.file.controller;

import com.example.goldencrow.file.service.ProjectService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

import static com.example.goldencrow.common.Constants.*;

/**
 * 프로젝트 관련 Controller
 *
 * @url /api/projects
 */
@RestController
@RequestMapping(value = "/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * ProjectController 생성자
     *
     * @param projectService project 관련 로직을 처리하는 Service
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 프로젝트 디렉토리 조회 API
     *
     * @param teamSeq 조회하려는 프로젝트의 팀 Sequence
     * @return 디렉토리 구조를 반환
     * @status 200, 400, 401
     */
    @GetMapping("/directories/{teamSeq}")
    public ResponseEntity<Map<Object, Object>> pjtReadGet(@PathVariable Long teamSeq) {
        String pjtPath = BASE_URL + teamSeq;
        File teamPjt = new File(pjtPath);
        File[] files = teamPjt.listFiles();

        Map<Object, Object> res = new HashMap<>();
        if (files == null) {
            res.put("result", BAD_REQ);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } else if (files.length == 0) {
            res.put("result", BAD_REQ);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        pjtPath = pjtPath.replace(BASE_URL,"");
        for (File rootFile : files) {
            if (rootFile.getName().equals("DockerFile")) {
                continue;
            }
            String rootPath = rootFile.getPath().replace(BASE_URL, "");
            res = projectService.readDirectoryService(rootPath,rootFile.getName(),res);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
