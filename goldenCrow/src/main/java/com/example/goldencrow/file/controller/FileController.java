package com.example.goldencrow.file.controller;

import com.example.goldencrow.file.dto.FileCreateRequestDto;
import com.example.goldencrow.file.service.FileService;
import com.example.goldencrow.user.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * 파일을 관리하는 Controller
 *
 * @url /api/files
 */
@RestController
@RequestMapping(value = "/api/files")
public class FileController {
    private final FileService fileService;
    private final JwtService jwtService;

    /**
     * FileController 생성자
     *
     * @param fileService file 관련 로직을 처리하는 Service
     * @param jwtService  access token 관련 로직을 처리하는 Service
     */
    public FileController(FileService fileService, JwtService jwtService) {
        this.fileService = fileService;
        this.jwtService = jwtService;
    }

    /**
     * 파일(폴더) 생성 API
     *
     * @param teamSeq              파일(폴더)를 생성할 팀의 sequence
     * @param type                 생성할 문서의 종류 (1 : 폴더, 2 : 파일)
     * @param fileCreateRequestDto "fileTitle", "filePath"를 key로 가지는 Dto
     * @return 파일(폴더) 생성 성공 시 파일 경로 반환, 성패에 대한 result 반환
     * @status 200, 400, 401
     */
    @PostMapping("/{teamSeq}")
    public ResponseEntity<Map<String, String>> userFileCreatePost(@PathVariable Long teamSeq,
                                                                  @RequestParam int type,
                                                                  @RequestBody FileCreateRequestDto fileCreateRequestDto) {
        Map<String, String> res = fileService.createFileService(teamSeq, type, fileCreateRequestDto);
        if (res.get("result").equals(SUCCESS)) {
            return new ResponseEntity<>(res, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 파일(폴더) 삭제 API
     *
     * @param teamSeq 파일(폴더)를 삭제할 팀의 sequence
     * @param type    삭제할 문서의 종류 (1 : 폴더, 2 : 파일)
     * @param req     "filePath"를 key로 가지는 Map<String, String>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @DeleteMapping("/{teamSeq}")
    public ResponseEntity<Map<String, String>> userFileDelete(@PathVariable Long teamSeq,
                                                              @RequestParam int type,
                                                              @RequestBody Map<String, String> req) {
        if (req.containsKey("filePath")) {
            String filePath = req.get("filePath");
            Map<String, String> res = fileService.deleteFileService(BASE_URL + filePath, type, teamSeq);
            switch (res.get("result")) {
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

    /**
     * 파일 이름 변경 API
     *
     * @param teamSeq 이름을 변경하려는 파일(폴더)의 프로젝트(팀)의 Sequence
     * @param req     "filePath", "oldFileName", "fileTitle"을 key로 가지는 Map<String, String>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PutMapping("/{teamSeq}/file-title")
    public ResponseEntity<Map<String, String>> fileNameUpdatePut(@PathVariable Long teamSeq,
                                                                 @RequestBody Map<String, String> req) {
        if (req.containsKey("filePath") && req.containsKey("oldFileName") && req.containsKey("fileTitle")) {
            String filePath = req.get("filePath");
            String fileTitle = req.get("fileTitle");
            String oldFileName = req.get("oldFileName");

            Map<String, String> res =
                    fileService.updateFileNameService(BASE_URL + filePath, fileTitle, oldFileName, teamSeq);
            switch (res.get("result")) {
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

    /**
     * @param jwt
     * @param teamSeq
     * @return
     */
    @GetMapping("/{teamSeq}")
    public ResponseEntity<String> getDirectory(@RequestHeader("Authorization") String jwt, @PathVariable Long teamSeq) {
        Long result = jwtService.JWTtoUserSeq(jwt);
        return new ResponseEntity<>(String.valueOf(result), HttpStatus.OK);
    }

    /**
     * 파일 저장 API
     *
     * @param teamSeq 파일을 저장할 프로젝트(팀)의 Sequence
     * @param req     "fileContent", "filePath"를 key로 가지는 Map<String, String>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PutMapping("/{teamSeq}/files")
    public ResponseEntity<Map<String, String>> saveFilePut(@PathVariable Long teamSeq, @RequestBody Map<String, String> req) {
        if (req.containsKey("fileContent") && req.containsKey("filePath")) {
            String fileContent = req.get("fileContent");
            String filePath = req.get("filePath");
            Map<String, String> res = fileService.saveFileService(BASE_URL + filePath, fileContent);
            switch (res.get("result")) {
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

    /**
     * 파일 내용을 조회하는 API
     *
     * @param req "filePath"를 key로 가지는 Map<String, String>
     * @return 파일의 내용 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PostMapping("/files")
    public ResponseEntity<Map<String, String>> readFilePost(@RequestBody Map<String, String> req) {
        if (req.containsKey("filePath")) {
            String filePath = req.get("filePath");
            Map<String, String> res = fileService.readFileService(BASE_URL + filePath);
            switch (res.get("result")) {
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
