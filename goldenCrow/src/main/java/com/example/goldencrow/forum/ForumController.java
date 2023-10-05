package com.example.goldencrow.forum;

import com.example.goldencrow.forum.dto.ForumDto;
import com.example.goldencrow.forum.dto.ForumPageDto;
import com.example.goldencrow.user.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final String SUCCESS = "SUCCESS";
    private final String FAILURE = "FAILURE";
    private final ForumService forumService;
    private final JwtService jwtService;

    private final int onePage = 8;

    public ForumController(ForumService forumService, JwtService jwtService) {
        this.forumService = forumService;
        this.jwtService = jwtService;
    }

    // 게시글 등록
    @PostMapping("")
    public ResponseEntity<String> ForumUpload(@RequestHeader("Authorization") String jwt, @RequestBody ForumDto forumDto) {
        if (jwt.isEmpty()) {
            return new ResponseEntity<>(FAILURE, HttpStatus.BAD_REQUEST);
        } else {
            Long userSeq = jwtService.JWTtoUserSeq(jwt);
            String result = forumService.forumUpload(forumDto, userSeq);
            if (result.equals("success")) {
                return new ResponseEntity<>(SUCCESS, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(FAILURE, HttpStatus.BAD_REQUEST);
            }
        }
    }

    // 모든 게시글 읽기
    @GetMapping("/read")
    public ResponseEntity<ForumPageDto> forumListRecent(
//            @RequestParam("pages") int page
    ) {
        ForumPageDto forumPageDto = forumService.forumAll();
        return new ResponseEntity<>(forumPageDto, HttpStatus.OK);
    }


    // 게시글 하나 읽기
    @GetMapping("/read/{boardSeq}")
    public ResponseEntity<ForumDto> forumOne(@PathVariable Long boardSeq) {

        ForumDto forumDto;

        forumDto = forumService.forumOne(boardSeq);

        if (forumDto.getResult() == ForumDto.ForumResult.SUCCESS) {
            return new ResponseEntity<>(forumDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{boardSeq}")
    public ResponseEntity<String> forumUpdate(@PathVariable Long boardSeq, @RequestHeader("Authorization") String jwt, @RequestBody ForumDto forumDto) {
        if (jwt.isEmpty()) {
            return new ResponseEntity<>(FAILURE, HttpStatus.UNAUTHORIZED);
        } else {
            Long userSeq = jwtService.JWTtoUserSeq(jwt);
            if (forumService.forumCheck(userSeq, boardSeq).equals("success")) {
                String result = forumService.forumUpdate(forumDto, boardSeq);
                System.out.println(result);

                if (result.equals("success")) {
                    return new ResponseEntity<>(SUCCESS, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(FAILURE, HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(FAILURE, HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @DeleteMapping("/{boardSeq}")
    public ResponseEntity<String> forumDelete(@PathVariable Long boardSeq,
                                              @RequestHeader("Authorization") String jwt) {
        if (jwt.isEmpty()) {
            return new ResponseEntity<>(FAILURE, HttpStatus.UNAUTHORIZED);
        } else {
            Long userSeq = jwtService.JWTtoUserSeq(jwt);
            if (forumService.forumCheck(userSeq, boardSeq).equals("success")) {
                String result = forumService.forumDelete(boardSeq);
                if (result.equals("success")) {
                    return new ResponseEntity<>(SUCCESS, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(FAILURE, HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(FAILURE, HttpStatus.UNAUTHORIZED);
            }
        }
    }
}
