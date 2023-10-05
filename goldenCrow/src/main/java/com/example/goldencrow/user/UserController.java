package com.example.goldencrow.user;

import com.example.goldencrow.user.dto.MyInfoDto;
import com.example.goldencrow.user.dto.SettingsDto;
import com.example.goldencrow.user.dto.UserInfoDto;
import com.example.goldencrow.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * 사용자와 관련된 입출력을 처리하는 controller
 *
 * @url /api/users
 */
@RestController
@RequestMapping(value = "/api/users")
public class UserController {

    private final UserService userService;

    /**
     * UserController 생성자
     *
     * @param userService user를 관리하는 service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 회원가입 API
     *
     * @param req "userId", "userPassword", "userNickname"을 key로 가지는 Map<String, String>
     * @return 회원가입 성공 시 jwt 반환, 성패에 따른 result 반환
     * @status 200, 400, 409
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signupPost(@RequestBody Map<String, String> req) {

        if (req.containsKey("userId") && req.containsKey("userPassword") && req.containsKey("userNickname")) {

            String userId = req.get("userId");
            String userPassword = req.get("userPassword");
            String userNickname = req.get("userNickname");

            Map<String, String> res = userService.signupService(userId, userPassword, userNickname);
            String result = res.get("result");

            switch (result) {
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

    /**
     * 로그인 API
     *
     * @param req "userId", "userPassword"를 key로 가지는 Map<String, String>
     * @return 로그인 성공 시 jwt 반환, 성패에 따른 result 반환
     * @status 200, 400, 409
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginPost(@RequestBody Map<String, String> req) {

        if (req.containsKey("userId") && req.containsKey("userPassword")) {

            String userId = req.get("userId");
            String userPassword = req.get("userPassword");

            Map<String, String> res = userService.loginService(userId, userPassword);
            String result = res.get("result");

            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case WRONG:
                case NO_SUCH:
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
     * 각 회원의 정보를 조회하는 API
     * access token 필요
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 조회 성공 시 당사자가 접근 가능한 사용자 정보 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @GetMapping("/info")
    public ResponseEntity<MyInfoDto> myInfoGet(@RequestHeader("Authorization") String jwt) {

        MyInfoDto myInfoDtoRes = userService.myInfoService(jwt);
        String result = myInfoDtoRes.getResult();

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(myInfoDtoRes, HttpStatus.OK);
            case NO_SUCH:
                return new ResponseEntity<>(myInfoDtoRes, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(myInfoDtoRes, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 사용자 닉네임을 수정하는 API
     * access token 필요
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @param req "userNickname"을 key로 가지는 Map<String, String>
     * @return 수정 성공 시 userNickname 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PutMapping("/edit/nickname")
    public ResponseEntity<Map<String, String>> editNicknamePut(@RequestHeader("Authorization") String jwt,
                                                               @RequestBody Map<String, String> req) {

        if (req.containsKey("userNickname")) {

            String userNickname = req.get("userNickname");

            Map<String, String> res = userService.editNicknameService(jwt, userNickname);
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

    /**
     * 프로필 사진을 수정하는 API
     *
     * @param jwt           회원가입 및 로그인 시 발급되는 access token
     * @param multipartFile 프로필 사진으로 사용할 jpg 이미지 파일
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    @PutMapping("/edit/profile")
    public ResponseEntity<Map<String, String>> editProfilePut(@RequestHeader("Authorization") String jwt,
                                                              @RequestBody MultipartFile multipartFile) {

        if (multipartFile.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        Map<String, String> res = userService.editProfileService(jwt, multipartFile);
        String result = res.get("result");

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(res, HttpStatus.OK);
            case NO_SUCH:
                return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 프로필 사진을 삭제하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    @DeleteMapping("/edit/profile")
    public ResponseEntity<Map<String, String>> deleteProfileDelete(@RequestHeader("Authorization") String jwt) {

        Map<String, String> res = userService.deleteProfileService(jwt);
        String result = res.get("result");

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(res, HttpStatus.OK);
            case NO_SUCH:
                return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 사용자의 Git 사용자명, 토큰을 수정하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @param req "userGitUsername", "userGitToken"를 key로 가지는 Map<String, String>
     * @return 수정 성공 시 userGitUsername 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 404
     */
    @PutMapping("/edit/git")
    public ResponseEntity<Map<String, String>> editGitPut(@RequestHeader("Authorization") String jwt,
                                                          @RequestBody Map<String, String> req) {

        if (req.containsKey("userGitUsername") && req.containsKey("userGitToken")) {

            String userGitUsername = req.get("userGitUsername");
            String userGitToken = req.get("userGitToken");

            Map<String, String> res = userService.editGitService(jwt, userGitUsername, userGitToken);
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

    /**
     * 비밀번호를 수정하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @param req "userPassword", "userNewPassword"를 key로 가지는 Map<String, String>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404, 409
     */
    @PutMapping("/edit/password")
    public ResponseEntity<Map<String, String>> editPasswordPut(@RequestHeader("Authorization") String jwt,
                                                               @RequestBody Map<String, String> req) {

        if (req.containsKey("userPassword") && req.containsKey("userNewPassword")) {

            String userPassword = req.get("userPassword");
            String userNewPassword = req.get("userNewPassword");

            Map<String, String> res = userService.editPasswordService(jwt, userPassword, userNewPassword);
            String result = res.get("result");

            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_SUCH:
                    return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
                case WRONG:
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
     * 회원 탈퇴 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404
     */
    @DeleteMapping("/quit")
    public ResponseEntity<Map<String, String>> quitDelete(@RequestHeader("Authorization") String jwt) {

        Map<String, String> res = userService.quitService(jwt);
        String result = res.get("result");

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(res, HttpStatus.OK);
            case NO_PER:
                return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
            case NO_SUCH:
                return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 사용자별로 개인 환경 세팅을 저장하는 API
     *
     * @param jwt         회원가입 및 로그인 시 발급되는 access token
     * @param settingsDto 개인 환경 세팅 정보
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404
     */
    @PutMapping("/personal/{teamSeq}")
    public ResponseEntity<Map<String, String>> personalPost(@RequestHeader("Authorization") String jwt,
                                                            @PathVariable Long teamSeq,
                                                            @RequestBody SettingsDto settingsDto) {

        Map<String, String> res = userService.personalPostService(jwt, teamSeq, settingsDto);
        String result = res.get("result");

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(res, HttpStatus.OK);
            case NO_PER:
                return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
            case NO_SUCH:
                return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 사용자별 개인 환경 세팅을 조회하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 조회 성공 시 개인 환경 세팅 정보 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404
     */
    @GetMapping("/personal/{teamSeq}")
    public ResponseEntity<SettingsDto> personalGet(@RequestHeader("Authorization") String jwt,
                                                   @PathVariable Long teamSeq) {

        SettingsDto settingsDtoRes = userService.personalGetService(jwt, teamSeq);
        String result = settingsDtoRes.getResult();

        switch (result) {
            case SUCCESS:
            case "NO_VALUE":
                return new ResponseEntity<>(settingsDtoRes, HttpStatus.OK);
            case NO_PER:
                return new ResponseEntity<>(settingsDtoRes, HttpStatus.FORBIDDEN);
            case NO_SUCH:
                return new ResponseEntity<>(settingsDtoRes, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(settingsDtoRes, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 사용자의 프로필을 UserSeq로 조회하는 API
     *
     * @param userSeq 프로필을 조회하고자 하는 사용자의 UserSeq
     * @return 조회 성공 시 외부인이 접근 가능한 사용자 정보 반환, 성패에 따른 result 반환
     * @status 200, 400, 404
     */
    @GetMapping("/mypage/{userSeq}")
    public ResponseEntity<UserInfoDto> mypageGet(@PathVariable Long userSeq) {

        UserInfoDto userInfoDtoRes = userService.mypageService(userSeq);
        String result = userInfoDtoRes.getResult();

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(userInfoDtoRes, HttpStatus.OK);
            case NO_SUCH:
                return new ResponseEntity<>(userInfoDtoRes, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(userInfoDtoRes, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 사용자를 검색하는 API
     *
     * @param req "searchWord"를 key로 가지는 Map<String, String>
     * @return 조회 성공 시 searchWord가 Id 혹은 Nickname에 포함되는 사용자의 리스트를 반환
     * @status 200, 400
     */
    @PostMapping("/search")
    public ResponseEntity<List<UserInfoDto>> searchUserGet(@RequestBody Map<String, String> req) {

        if (req.containsKey("searchWord")) {

            String searchWord = req.get("searchWord");
            List<UserInfoDto> userInfoDtoList = userService.searchUser(searchWord);

            if(userInfoDtoList==null) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(userInfoDtoList, HttpStatus.OK);
            }

        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);

        }

    }
}
