package com.example.goldencrow.team;

import com.example.goldencrow.team.dto.TeamDto;
import com.example.goldencrow.team.dto.UserInfoListDto;
import com.example.goldencrow.user.dto.UserInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * 팀과 관련된 입출력을 처리하는 controller
 *
 * @url /api/teams
 */
@RestController
@RequestMapping(value = "/api/teams")
public class TeamController {

    private final TeamService teamService;

    /**
     * TeamController 생성자
     *
     * @param teamService team을 관리하는 service
     */
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 사용자가 속한 팀 목록을 조회하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 조회 성공 시 사용자가 속한 팀의 리스트를 반환
     * @status 200, 400, 401, 404
     */
    @GetMapping("")
    public ResponseEntity<List<TeamDto>> teamListGet(@RequestHeader("Authorization") String jwt) {

        List<TeamDto> listTeamDto = teamService.teamListService(jwt);

        if (listTeamDto == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(listTeamDto, HttpStatus.OK);
        }

    }

    /**
     * 팀의 세부 정보를 조회하는 API
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 조회하고자 하는 팀의 Seq
     * @return 조회 성공 시 해당 팀의 정보를 반환
     * @status 200, 400, 401, 403, 404
     */
    @GetMapping("/{teamSeq}")
    public ResponseEntity<TeamDto> teamGet(@RequestHeader("Authorization") String jwt, @PathVariable Long teamSeq) {

        TeamDto teamDto = teamService.teamGetService(jwt, teamSeq);
        String result = teamDto.getResult();

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(teamDto, HttpStatus.OK);
            case NO_PER:
                return new ResponseEntity<>(teamDto, HttpStatus.FORBIDDEN);
            case NO_SUCH:
                return new ResponseEntity<>(teamDto, HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(teamDto, HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 팀을 생성하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @param req "teamName", "projectType", "teamGit"을 key로 가지는 Map<String, String>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 404, 409
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> teamCreatePost(@RequestHeader("Authorization") String jwt,
                                                              @RequestBody Map<String, String> req) {

        if (req.containsKey("teamName") && req.containsKey("projectType") && req.containsKey("teamGit")) {

            String teamName = req.get("teamName");
            String projectType = req.get("projectType");
            String teamGit = req.get("teamGit");

            Map<String, String> res = teamService.teamCreateService(jwt, teamName, projectType, teamGit);
            String result = res.get("result");

            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_PER:
                    return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
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
     * 팀명을 수정하는 API
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 팀명을 바꾸고자 하는 팀의 Seq
     * @param req     "teamName"을 key로 가지는 Map<String, String>
     * @return 성공 시 수정된 팀명 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404, 409
     */
    @PutMapping("/modify/name/{teamSeq}")
    public ResponseEntity<Map<String, String>> teamModifyNamePut(@RequestHeader("Authorization") String jwt,
                                                                 @PathVariable Long teamSeq, @RequestBody Map<String, String> req) {

        if (req.containsKey("teamName")) {

            String teamName = req.get("teamName");

            Map<String, String> res = teamService.teamModifyNameService(jwt, teamSeq, teamName);
            String result = res.get("result");

            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_PER:
                    return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
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
     * 팀의 Git을 수정하는 API
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq Git을 바꾸고자 하는 팀의 Seq
     * @param req     "teamGit"을 key로 가지는 Map<String, String>
     * @return 성공 시 수정된 Git 주소 반환, 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404
     */
    @PutMapping("/modify/git/{teamSeq}")
    public ResponseEntity<Map<String, String>> teamModifyGitPut(@RequestHeader("Authorization") String jwt,
                                                                @PathVariable Long teamSeq, @RequestBody Map<String, String> req) {

        if (req.containsKey("teamGit")) {

            String teamGit = req.get("teamGit");

            Map<String, String> res = teamService.teamModifyGitService(jwt, teamSeq, teamGit);
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

        } else {
            Map<String, String> res = new HashMap<>();
            res.put("result", BAD_REQ);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);

        }

    }

    /**
     * 팀을 삭제하는 API
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 삭제하고자 하는 팀의 Seq
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404
     */
    @DeleteMapping("/delete/{teamSeq}")
    public ResponseEntity<Map<String, String>> teamDelete(@RequestHeader("Authorization") String jwt,
                                                          @PathVariable Long teamSeq) {

        Map<String, String> res = teamService.teamDeleteService(jwt, teamSeq);
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
     * 팀의 팀원 목록을 조회하는 API
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 조회하고자 하는 팀의 Seq
     * @return 팀의 팀원 목록 리스트 반환
     * @status 200, 400, 401, 403, 404
     */
    @GetMapping("/member/{teamSeq}")
    public ResponseEntity<List<UserInfoDto>> memberListGet(@RequestHeader("Authorization") String jwt,
                                                           @PathVariable Long teamSeq) {

        UserInfoListDto res = teamService.memberListService(jwt, teamSeq);
        String result = res.getResult();

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(res.getUserInfoDtoList(), HttpStatus.OK);
            case NO_PER:
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.FORBIDDEN);
            case NO_SUCH:
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * 팀에 팀원을 추가하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @param req "teamSeq"와 "memberSeq"를 key로 가지는 Map<String, Long>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404, 409
     */
    @PutMapping("/add")
    public ResponseEntity<Map<String, String>> memberAddPut(@RequestHeader("Authorization") String jwt, @RequestBody Map<String, Long> req) {

        if (req.containsKey("teamSeq") && req.containsKey("memberSeq")) {

            Long teamSeq = req.get("teamSeq");
            Long memberSeq = req.get("memberSeq");

            Map<String, String> res = teamService.memberAddService(jwt, teamSeq, memberSeq);
            String result = res.get("result");

            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_PER:
                    return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
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
     * 팀원을 삭제하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @param req "teamSeq"와 "memberSeq"를 key로 가지는 Map<String, Long>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404, 409
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> memberRemoveDelete(@RequestHeader("Authorization") String jwt, @RequestBody Map<String, Long> req) {

        if (req.containsKey("teamSeq") && req.containsKey("memberSeq")) {

            Long teamSeq = req.get("teamSeq");
            Long memberSeq = req.get("memberSeq");

            Map<String, String> res = teamService.memberRemoveService(jwt, teamSeq, memberSeq);
            String result = res.get("result");

            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_PER:
                    return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
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
     * 팀의 팀장을 위임하는 API
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @param req "teamSeq"와 "memberSeq"를 key로 가지는 Map<String, Long>
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404, 409
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    @PutMapping("/beLeader")
    public ResponseEntity<Map<String, String>> memberBeLeaderPut(@RequestHeader("Authorization") String jwt, @RequestBody Map<String, Long> req) {

        if (req.containsKey("teamSeq") && req.containsKey("memberSeq")) {

            Long teamSeq = req.get("teamSeq");
            Long memberSeq = req.get("memberSeq");

            Map<String, String> res = teamService.memberBeLeaderService(jwt, teamSeq, memberSeq);
            String result = res.get("result");

            switch (result) {
                case SUCCESS:
                    return new ResponseEntity<>(res, HttpStatus.OK);
                case NO_PER:
                    return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
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
     * 팀을 탈퇴하는 API
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 탈퇴하고자하는 팀의 Seq
     * @return 성패에 따른 result 반환
     * @status 200, 400, 401, 403, 404, 409
     */
    @DeleteMapping("/quit/{teamSeq}")
    public ResponseEntity<Map<String, String>> memberQuitDelete(@RequestHeader("Authorization") String jwt, @PathVariable Long teamSeq) {

        Map<String, String> res = teamService.memberQuitService(jwt, teamSeq);
        String result = res.get("result");

        switch (result) {
            case SUCCESS:
                return new ResponseEntity<>(res, HttpStatus.OK);
            case NO_PER:
                return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
            case NO_SUCH:
                return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
            case WRONG:
                return new ResponseEntity<>(res, HttpStatus.CONFLICT);
            default:
                return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

}
