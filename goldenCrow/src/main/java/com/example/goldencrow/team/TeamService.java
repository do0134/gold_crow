package com.example.goldencrow.team;

import com.example.goldencrow.compile.CompileService;
import com.example.goldencrow.file.service.ProjectService;
import com.example.goldencrow.git.GitService;
import com.example.goldencrow.team.dto.MemberDto;
import com.example.goldencrow.team.dto.TeamDto;
import com.example.goldencrow.team.dto.UserInfoListDto;
import com.example.goldencrow.team.entity.MemberEntity;
import com.example.goldencrow.team.entity.TeamEntity;
import com.example.goldencrow.team.repository.MemberRepository;
import com.example.goldencrow.team.repository.TeamRepository;
import com.example.goldencrow.user.service.JwtService;
import com.example.goldencrow.user.UserEntity;
import com.example.goldencrow.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.goldencrow.common.Constants.*;

/**
 * team을 관리하는 service
 */
@Service
public class TeamService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;

    private final JwtService jwtService;
    private final ProjectService projectService;
    private final CompileService compileService;
    private final GitService gitService;


    /**
     * TeamService 생성자
     *
     * @param userRepository   User Table에 접속하는 repository
     * @param teamRepository   Team Table에 접속하는 repository
     * @param memberRepository Member table에 접속하는 repository
     * @param jwtService       jwt를 관리하는 service
     * @param projectService   project를 관리하는 service
     * @param compileService   compile을 관리하는 service
     * @param gitService       git을 관리하는 service
     */
    public TeamService(UserRepository userRepository, TeamRepository teamRepository, MemberRepository memberRepository,
                       JwtService jwtService, ProjectService projectService, CompileService compileService,
                       GitService gitService) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.jwtService = jwtService;
        this.projectService = projectService;
        this.compileService = compileService;
        this.gitService = gitService;
    }

    /**
     * 사용자가 속한 팀 목록을 조회하는 내부 로직
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 조회 성공 시 사용자가 속한 팀의 리스트를 반환
     */
    public List<TeamDto> teamListService(String jwt) {

        try {

            // jwt에서 UserSeq 추출
            Long userSeq = jwtService.JWTtoUserSeq(jwt);

            // 사용자의 UserSeq를 가지는 MemberEntity를 리스트로 추출
            List<MemberEntity> userMemberEntityList = memberRepository.findAllByUser_UserSeq(userSeq);

            // 내보내기 위한 List<TeamDto> 생성
            List<TeamDto> listTeamDto = new ArrayList<>();

            // 멤버 리스트가 비었는지 확인
            if (!userMemberEntityList.isEmpty()) {

                // 비지 않았을 경우, 각 멤버가 속한 팀의 정보를 작성함

                for (MemberEntity m : userMemberEntityList) {
                    // MemberEntity로 TeamDto를 반환하는 서비스 호출
                    // 반환된 TeamDto를 리스트에 삽입
                    listTeamDto.add(teamReadService(m));
                }

            }

            return listTeamDto;

        } catch (Exception e) {
            return null;

        }

    }

    /**
     * 팀의 세부 정보를 조회하는 API
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 조회하고자 하는 팀의 Seq
     * @return 조회 성공 시 해당 팀의 정보를 반환
     */
    public TeamDto teamGetService(String jwt, Long teamSeq) {

        try {

            // jwt에서 UserSeq 추출
            Long userSeq = jwtService.JWTtoUserSeq(jwt);

            // 입력받은 TeamSeq를 가지는 TeamEntity를 추출
            Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeq(teamSeq);

            // 존재하는 팀인지 확인
            if (!teamEntityOptional.isPresent()) {
                // 존재하지 않을 경우 탐색 종료
                TeamDto teamDto = new TeamDto();
                teamDto.setResult(NO_SUCH);
                return teamDto;
            }

            // 사용자가 해당 팀에 속해 있는지를 확인
            // 해당 사용자와 해당 팀으로 이루어진 MemberEntity를 추출
            Optional<MemberEntity> memberEntityOptional =
                    memberRepository.findByUser_UserSeqAndTeam_TeamSeq(userSeq, teamSeq);

            // 조건에 맞는 MemberEntity가 존재하는지 확인
            if (memberEntityOptional.isPresent()) {
                // 존재할 경우 : 사용자는 그 팀에 속해있음
                // 즉, 조회 권한이 있음
                // MemberEntity로 TeamDto를 반환하는 서비스 호출
                return teamReadService(memberEntityOptional.get());

            } else {
                // 존재할 경우 : 사용자는 그 팀에 속해있지 않음
                // 즉, 조회 권한이 없음, 탐색 종료
                TeamDto teamDto = new TeamDto();
                teamDto.setResult(NO_PER);
                return teamDto;

            }

        } catch (Exception e) {
            TeamDto teamDto = new TeamDto();
            teamDto.setResult(UNKNOWN);
            return teamDto;

        }

    }

    /**
     * 입력받은 멤버가 속한 팀 정보를 반환하는 service
     *
     * @param memberEntity 소속된 팀의 정보를 얻고 싶은 MemberEntity
     * @return 해당 멤버가 속한 팀 정보 반환
     */
    public TeamDto teamReadService(MemberEntity memberEntity) {

        // 해당 팀에 접근함
        // 그 팀의 팀장이 누군지를 기록함
        TeamEntity teamEntity = memberEntity.getTeam();
        Long leaderSeq = teamEntity.getTeamLeader().getUserSeq();

        // 내보내기 위한 List<MemberDto> 생성
        List<MemberDto> memberDtoList = new ArrayList<>();

        // 팀장을 제외한 멤버 리스트를 추출
        List<MemberEntity> memberEntityList = memberRepository.findAllByTeam_TeamSeq(teamEntity.getTeamSeq());
        for (MemberEntity mm : memberEntityList) {

            // 팀장이 아닌 멤버만 리스트에 추가
            if (!mm.getUser().getUserSeq().equals(leaderSeq)) {
                memberDtoList.add(new MemberDto(mm));
            }

        }

        // TeamDto 생성
        // 작성된 멤버 리스트를 TeadmDto에 기록
        // 완성된 TeamDto를 리스트에 기록
        TeamDto teamDto = new TeamDto(teamEntity);
        teamDto.setMemberDtoList(memberDtoList);
        teamDto.setResult(SUCCESS);
        return teamDto;

    }

    /**
     * 팀을 생성하는 내부 로직
     *
     * @param jwt         회원가입 및 로그인 시 발급되는 access token
     * @param teamName    만들고자 하는 팀의 이름
     * @param projectType 해당 팀에서 작업할 프로젝트의 종류
     * @param teamGit     git clone을 받아 프로젝트를 초기화할 경우, clone 받을 프로젝트의 git 주소
     * @return 팀 생성 성공 시 TeamSeq 반환, 성패에 따른 result 반환
     */
    public Map<String, String> teamCreateService(String jwt, String teamName, String projectType, String teamGit) {

        Map<String, String> serviceRes = new HashMap<>();

        // String으로 입력받은 projectType을 내부 로직용으로 int로 치환
        int typeNum;
        switch (projectType) {
            case "pure Python":
                typeNum = 1;
                break;
            case "Django":
                typeNum = 2;
                break;
            case "Flask":
                typeNum = 3;
                break;
            case "FastAPI":
                typeNum = 4;
                break;
            default:
                serviceRes.put("result", WRONG);
                return serviceRes;
        }

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));
            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();

            Optional<TeamEntity> teamEntityOptional
                    = teamRepository.findTeamEntityByTeamLeader_UserSeqAndTeamName(userEntity.getUserSeq(), teamName);

            // 같은 팀장, 같은 팀명인 팀이 기존에 존재하는지 확인
            if (teamEntityOptional.isPresent()) {
                // 해당하는 팀이 존재함, 생성 불가
                serviceRes.put("result", DUPLICATE);
                return serviceRes;
            }

            // 사용자를 팀장으로 하는 팀 생성
            TeamEntity teamEntity = new TeamEntity(userEntity, teamName, teamGit, typeNum);
            teamRepository.saveAndFlush(teamEntity);
            Long teamSeq = teamEntity.getTeamSeq();
            System.out.println("말좀해봐뭐가문제야:"+teamEntity);

            // 팀의 프로젝트에 대한 컨테이너 생성
            Map<String, String> containerRes = compileService.containerCreateService(teamName, teamSeq);
            if (!containerRes.get("result").equals(SUCCESS)) {
                // 생성에 실패함
                return containerRes;

            }

            System.out.println("컨테이너 생성 성공");

            // git clone을 받아오는지, 새로 생성하는지 판별
            if (teamGit == null) {

                System.out.println("깃정보가 비었음이 확인됨");

                // git 정보가 비어있는 상태이므로 클론을 받아오지 않고, 프로젝트를 생성함
                Map<String, String> projectCreateRes
                        = projectService.createProjectService(typeNum, teamName, teamSeq);

                if (projectCreateRes.get("result").equals(SUCCESS)) {
                    // 성공
                    serviceRes.put("result", SUCCESS);
                    serviceRes.put("teamSeq", String.valueOf(teamSeq));

                    System.out.println("생성이 성공함");

                } else {
                    // 모든 경우의 프로젝트 생성 실패
                    System.out.println("여기서?터졋나?");

                    // 미리 만들어두었던 팀을 삭제
                    // 팀 삭제와 함께 멤버도 자동으로 삭제됨
                    teamRepository.delete(teamEntity);

                    return projectCreateRes;

                }

            } else {

                // 쓰여진 주소에서 정보를 받아와 프로젝트를 구축함
                Map<String, String> gitCloneRes = gitService.gitCloneService(teamGit, teamSeq, teamName);

                if (gitCloneRes.get("result").equals(SUCCESS)) {
                    // 성공
                    serviceRes.put("result", SUCCESS);
                    serviceRes.put("teamSeq", String.valueOf(teamSeq));

                } else {

                    // 미리 만들어두었던 팀을 삭제
                    // 팀 삭제와 함께 멤버도 자동으로 삭제됨
                    teamRepository.delete(teamEntity);

                    return gitCloneRes;

                }

            }

            System.out.println("포트저장시작");

            teamEntity.setTeamPort(containerRes.get("port"));
            teamRepository.saveAndFlush(teamEntity);

            System.out.println("포트저장완료");

            // 만들어진 팀에 사용자를 멤버로 DB에 기록
            MemberEntity memberEntity = new MemberEntity(userEntity, teamEntity);
            memberRepository.saveAndFlush(memberEntity);

            System.out.println("멤버등록성공");

            serviceRes.put("result", SUCCESS);
            serviceRes.put("teamSeq", String.valueOf(teamSeq));

        } catch (Exception e) {
            System.out.println("언노운");
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 팀명을 수정하는 내부 로직
     *
     * @param jwt      회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq  팀명을 바꾸고자 하는 팀의 Seq
     * @param teamName 적용될 새로운 teamName
     * @return 성공 시 수정된 팀명 반환, 성패에 따른 result 반환
     */
    public Map<String, String> teamModifyNameService(String jwt, Long teamSeq, String teamName) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            // 그 팀의 팀장이 사용자가 맞는지 확인
            Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeqAndTeamLeader_UserSeq(teamSeq, userSeq);
            if (teamEntityOptional.isPresent()) {

                // 팀장과 팀 시퀀스가 일치하는 팀이 존재함

                // 해당 팀명을 사용했을 때, 팀장과 팀명이 모두 같은 팀이 있는지 확인
                Optional<TeamEntity> teamEntityConflictCheck
                        = teamRepository.findTeamEntityByTeamLeader_UserSeqAndTeamName(userSeq, teamName);
                if (teamEntityConflictCheck.isPresent()) {
                    // 중복되는 팀 리더와 팀 명이 있음
                    serviceRes.put("result", DUPLICATE);
                    return serviceRes;
                }

                TeamEntity teamEntity = teamEntityOptional.get();
                teamEntity.setTeamName(teamName);
                teamRepository.saveAndFlush(teamEntity);
                serviceRes.put("result", SUCCESS);

            } else {
                // 그 팀의 팀장이 사용자가 아니므로, 수정할 수 없음
                serviceRes.put("result", NO_PER);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 팀의 Git을 수정하는 내부 로직
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq Git을 바꾸고자 하는 팀의 Seq
     * @param teamGit 적용될 새로운 Git 주소
     * @return 성공 시 수정된 Git 주소 반환, 성패에 따른 result 반환
     */
    public Map<String, String> teamModifyGitService(String jwt, Long teamSeq, String teamGit) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            // 그 팀의 팀장이 사용자가 맞는지 확인
            Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeqAndTeamLeader_UserSeq(teamSeq, userSeq);
            if (teamEntityOptional.isPresent()) {
                // 팀장과 팀 시퀀스가 일치하는 팀이 존재함
                TeamEntity teamEntity = teamEntityOptional.get();
                teamEntity.setTeamGit(teamGit);
                teamRepository.saveAndFlush(teamEntity);
                serviceRes.put("result", SUCCESS);

            } else {
                // 그 팀의 팀장이 사용자가 아니므로, 수정할 수 없음
                serviceRes.put("result", NO_PER);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 팀을 삭제하는 내부 로직
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 삭제하고자 하는 팀의 Seq
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> teamDeleteService(String jwt, Long teamSeq) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            // 그 팀의 팀장이 사용자가 맞는지 확인
            Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeqAndTeamLeader_UserSeq(teamSeq, userSeq);

            if (teamEntityOptional.isPresent()) {

                // 팀장과 팀 시퀀스가 일치하는 팀이 존재함

                // 서버 내에 있는 해당 팀의 프로젝트 삭제
                List<Long> teamSeqList = new ArrayList<>();
                teamSeqList.add(teamSeq);
                if (projectService.deleteProjectService(teamSeqList).get("result").equals(UNKNOWN)) {
                    serviceRes.put("result", UNKNOWN);
                    return serviceRes;
                }

                // DB에 등록되었던 팀을 삭제
                // 해당 팀에 연결된 멤버도 자동으로 삭제
                TeamEntity teamEntity = teamEntityOptional.get();
                teamRepository.delete(teamEntity);
                serviceRes.put("result", SUCCESS);

            } else {
                // 그 팀의 팀장이 사용자가 아니므로, 수정할 수 없음
                serviceRes.put("result", NO_PER);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 팀의 팀원 목록을 조회하는 내부 로직
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 조회하고자 하는 팀의 Seq
     * @return 팀의 팀원 목록 리스트 반환
     */
    public UserInfoListDto memberListService(String jwt, Long teamSeq) {

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                UserInfoListDto userInfoListDto = new UserInfoListDto();
                userInfoListDto.setResult(NO_SUCH);
                return userInfoListDto;

            }

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                UserInfoListDto userInfoListDto = new UserInfoListDto();
                userInfoListDto.setResult(NO_SUCH);
                return userInfoListDto;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 사용자가 그 팀에 소속되어있는지 확인
            Optional<MemberEntity> memberEntityOptional
                    = memberRepository.findByUser_UserSeqAndTeam_TeamSeq(userSeq, teamSeq);

            if (memberEntityOptional.isPresent()) {
                // 내가 그 팀의 멤버로 있음이 확인되었으므로
                // 그 팀의 멤버 목록을 리스트로 받아옴
                List<MemberEntity> memberEntityList = memberRepository.findAllByTeam_TeamSeq(teamSeq);
                UserInfoListDto userInfoListDto = new UserInfoListDto(memberEntityList);
                userInfoListDto.setResult(SUCCESS);
                return userInfoListDto;

            } else {
                // 사용자가 그 팀의 멤버가 아니므로, 조회 불가
                UserInfoListDto userInfoListDto = new UserInfoListDto();
                userInfoListDto.setResult(NO_PER);
                return userInfoListDto;

            }

        } catch (Exception e) {
            UserInfoListDto userInfoListDto = new UserInfoListDto();
            userInfoListDto.setResult(UNKNOWN);
            return userInfoListDto;

        }

    }

    /**
     * 팀에 팀원을 추가하는 내부 로직
     *
     * @param jwt       회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq   추가하고자 하는 팀의 Seq
     * @param memberSeq 추가시킬 사용자의 User Seq
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> memberAddService(String jwt, Long teamSeq, Long memberSeq) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            // 그 팀의 팀장이 사용자가 맞는지 확인
            Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeqAndTeamLeader_UserSeq(teamSeq, userSeq);

            if (teamEntityOptional.isPresent()) {

                // 팀장과 팀 시퀀스가 일치하는 팀이 존재함

                // 추가하려는 팀원이 그 팀에 속해있지 않은지 확인
                Optional<MemberEntity> memberEntityOptional
                        = memberRepository.findByUser_UserSeqAndTeam_TeamSeq(memberSeq, teamSeq);

                if (!memberEntityOptional.isPresent()) {
                    // 팀원이 이 팀에 속해있지 않음
                    // DB에 멤버와 팀을 기록

                    Optional<UserEntity> memberUserEntityOptional = userRepository.findByUserSeq(memberSeq);

                    if (memberUserEntityOptional.isPresent()) {
                        // 팀에 해당 팀원을 추가함
                        UserEntity memberUserEntity = memberUserEntityOptional.get();
                        MemberEntity memberEntity = new MemberEntity(memberUserEntity, teamEntityOptional.get());
                        memberRepository.saveAndFlush(memberEntity);
                        serviceRes.put("result", SUCCESS);

                    } else {
                        // 해당 팀원은 존재하지 않는 유저임
                        serviceRes.put("result", NO_SUCH);

                    }

                } else {
                    // 그 팀원은 이미 해당 팀에 속해 있음
                    serviceRes.put("result", DUPLICATE);

                }

            } else {
                // 그 팀의 팀장이 사용자가 아니므로, 수정할 수 없음
                serviceRes.put("result", NO_PER);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);
        }

        return serviceRes;

    }

    /**
     * 팀원을 삭제하는 내부 로직
     *
     * @param jwt       회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq   삭제하고자 하는 팀의 Seq
     * @param memberSeq 삭제시킬 사용자의 User Seq
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> memberRemoveService(String jwt, Long teamSeq, Long memberSeq) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            // 그 팀의 팀장이 사용자가 맞는지 확인
            Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeqAndTeamLeader_UserSeq(teamSeq, userSeq);

            if (teamEntityOptional.isPresent()) {

                // 팀장과 팀 시퀀스가 일치하는 팀이 존재함

                // 추가하려는 팀원이 그 팀에 속해있지 않은지 확인
                Optional<MemberEntity> memberEntityOptional
                        = memberRepository.findByUser_UserSeqAndTeam_TeamSeq(memberSeq, teamSeq);

                if (memberEntityOptional.isPresent()) {
                    // 팀원이 이 팀에 속해있음
                    // DB의 해당 팀의 멤버를 삭제
                    memberRepository.delete(memberEntityOptional.get());
                    serviceRes.put("result", SUCCESS);
                } else {
                    // 그 팀원은 이미 해당 팀에 존재하지 않음
                    serviceRes.put("result", WRONG);
                }

            } else {
                // 그 팀의 팀장이 사용자가 아니므로, 수정할 수 없음
                serviceRes.put("result", NO_PER);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);
        }

        return serviceRes;

    }

    /**
     * 팀의 팀장을 위임하는 내부 로직
     *
     * @param jwt       회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq   삭제하고자 하는 팀의 Seq
     * @param memberSeq 삭제시킬 사용자의 User Seq
     * @return 성패에 따른 result 반환
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    public Map<String, String> memberBeLeaderService(String jwt, Long teamSeq, Long memberSeq) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            // 그 팀의 팀장이 사용자가 맞는지 확인
            Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeqAndTeamLeader_UserSeq(teamSeq, userSeq);

            if (teamEntityOptional.isPresent()) {

                // 팀장과 팀 시퀀스가 일치하는 팀이 존재함

                // 추가하려는 팀원이 그 팀에 속해있지 않은지 확인
                Optional<MemberEntity> memberEntityOptional
                        = memberRepository.findByUser_UserSeqAndTeam_TeamSeq(memberSeq, teamSeq);

                if (memberEntityOptional.isPresent()) {
                    // 팀원이 이 팀에 속해있음

                    Optional<UserEntity> memberUserEntityOptional = userRepository.findByUserSeq(memberSeq);

                    if (memberUserEntityOptional.isPresent()) {
                        // 그 팀원을 팀장으로 바꾸고, DB에 기록함
                        UserEntity memberUserEntity = memberUserEntityOptional.get();
                        TeamEntity teamEntity = teamEntityOptional.get();
                        teamEntity.setTeamLeader(memberUserEntity);
                        teamRepository.saveAndFlush(teamEntity);
                        serviceRes.put("result", SUCCESS);

                    } else {
                        // 해당 팀원은 존재하지 않는 유저임
                        serviceRes.put("result", NO_SUCH);

                    }

                } else {
                    // 그 팀원은 해당 팀에 속해 있지 않음
                    serviceRes.put("result", WRONG);
                }

            } else {
                // 그 팀의 팀장이 사용자가 아니므로, 수정할 수 없음
                serviceRes.put("result", NO_PER);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);
        }

        return serviceRes;

    }

    /**
     * 팀을 탈퇴하는 내부 로직
     *
     * @param jwt     회원가입 및 로그인 시 발급되는 access token
     * @param teamSeq 탈퇴하고자하는 팀의 Seq
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> memberQuitService(String jwt, Long teamSeq) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            UserEntity userEntity = userEntityOptional.get();
            Long userSeq = userEntity.getUserSeq();

            // 입력받은 팀이 존재하는지 확인
            Optional<TeamEntity> teamEntityFoundCheck = teamRepository.findByTeamSeq(teamSeq);
            if (!teamEntityFoundCheck.isPresent()) {
                // 해당하는 팀이 없음
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }

            // 그 팀에 사용자가 속해있는지 확인
            Optional<MemberEntity> memberEntityOptional
                    = memberRepository.findByUser_UserSeqAndTeam_TeamSeq(userSeq, teamSeq);

            if (memberEntityOptional.isPresent()) {

                // 사용자는 현재 그 팀에 속해 있음

                // 그 팀의 팀장이 사용자인지 확인
                Optional<TeamEntity> teamEntityOptional = teamRepository.findByTeamSeqAndTeamLeader_UserSeq(teamSeq, userSeq);

                if (!teamEntityOptional.isPresent()) {
                    // 사용자가 팀장이 아니므로, 탈퇴 가능함
                    MemberEntity memberEntity = memberEntityOptional.get();
                    memberRepository.delete(memberEntity);
                    serviceRes.put("result", SUCCESS);
                } else {
                    // 그 팀의 팀장이 사용자이므로, 탈퇴할 수 없음
                    serviceRes.put("result", NO_PER);
                }

            } else {
                // 사용자는 그 팀에 이미 속해있지 않음
                serviceRes.put("result", WRONG);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);
        }

        return serviceRes;

    }
}
