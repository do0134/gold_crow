package com.example.goldencrow.user.service;

import com.example.goldencrow.common.CryptoUtil;
import com.example.goldencrow.file.service.ProjectService;
import com.example.goldencrow.team.entity.MemberEntity;
import com.example.goldencrow.team.entity.TeamEntity;
import com.example.goldencrow.team.repository.MemberRepository;
import com.example.goldencrow.team.repository.TeamRepository;
import com.example.goldencrow.user.UserEntity;
import com.example.goldencrow.user.UserRepository;
import com.example.goldencrow.user.dto.MyInfoDto;
import com.example.goldencrow.user.dto.SettingsDto;
import com.example.goldencrow.user.dto.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.example.goldencrow.common.Constants.*;

/**
 * user를 관리하는 service
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;

    private final JwtService jwtService;
    private final ProjectService projectService;

    /**
     * UserService 생성자
     *
     * @param userRepository   User Table에 접속하는 repository
     * @param teamRepository   Team Table에 접속하는 repository
     * @param memberRepository Member Table에 접속하는 repository
     * @param jwtService       jwt를 관리하는 service
     * @param projectService   project를 관리하는 service
     */
    public UserService(UserRepository userRepository, TeamRepository teamRepository, MemberRepository memberRepository,
                       JwtService jwtService, ProjectService projectService) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.jwtService = jwtService;
        this.projectService = projectService;
    }

    /**
     * 회원가입 내부 로직
     *
     * @param userId       가입하려는 Id
     * @param userPassword 가입하려는 Password
     * @param userNickname 가입하려는 닉네임
     * @return 회원가입 성공 시 jwt 반환, 성패에 따른 result 반환
     */
    public Map<String, String> signupService(String userId, String userPassword, String userNickname) {

        Map<String, String> serviceRes = new HashMap<>();

        try {

            // 존재하는 Id인지 체크
            if (userRepository.findUserEntityByUserId(userId).isPresent()) {
                // user Table에 해당 Id가 이미 존재하므로 가입 불가
                serviceRes.put("result", DUPLICATE);

            } else {
                // user Table에 해당 Id가 등록되어 있지 않으므로 가입 진행

                // user Entity 생성
                // 비밀번호를 Sha256으로 인코딩해서 기록
                // DB에 등록
                UserEntity userEntity = new UserEntity(userId, userNickname);
                userEntity.setUserPassWord(CryptoUtil.Sha256.hash(userPassword));
                userRepository.saveAndFlush(userEntity);

                // 액세스 토큰 발급
                Long userSeq = userEntity.getUserSeq();
                String jwt = jwtService.createAccess(userSeq);
                serviceRes.put("result", SUCCESS);
                serviceRes.put("jwt", jwt);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 로그인 내부 로직
     *
     * @param userId       로그인 하려는 Id
     * @param userPassword 로그인 하려는 Password
     * @return 로그인 성공 시 jwt 반환, 성패에 따른 result 반환
     */
    public Map<String, String> loginService(String userId, String userPassword) {
        Map<String, String> serviceRes = new HashMap<>();

        try {
            // 존재하는 Id인지 체크
            if (userRepository.findUserEntityByUserId(userId).isPresent()) {
                // Id가 존재함을 확인함
                UserEntity userEntity = userRepository.findUserEntityByUserId(userId).get();
                String checkPassword = CryptoUtil.Sha256.hash(userPassword);

                // 비밀번호가 올바른지 확인함
                if (userEntity.getUserPassWord().equals(checkPassword)) {
                    // 인코딩한 비밀번호가 저장된 DB 정보와 같으면 로그인 성공
                    // 액세스 토큰 발급
                    String jwt = jwtService.createAccess(userEntity.getUserSeq());
                    serviceRes.put("result", SUCCESS);
                    serviceRes.put("jwt", jwt);

                } else {
                    // 저장된 DB 정보와 일치하지 않을 경우 로그인 실패
                    serviceRes.put("result", WRONG);
                }
            } else {
                // 가입되지 않은 Id임
                serviceRes.put("result", NO_SUCH);
            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 각 회원의 정보를 조회하는 내부 로직
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 당사자가 조회 가능한 사용자 정보 반환
     */
    public MyInfoDto myInfoService(String jwt) {

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                MyInfoDto myInfoDto = new MyInfoDto();
                myInfoDto.setResult(NO_SUCH);
                return myInfoDto;
            }

            UserEntity userEntity = userEntityOptional.get();

            // 내보내기 위한 MyInfoDto 생성
            MyInfoDto myInfoDto = new MyInfoDto(userEntity);


            // 위의 과정을 무사히 통과했으므로
            myInfoDto.setResult(SUCCESS);
            return myInfoDto;

        } catch (Exception e) {
            MyInfoDto myInfoDto = new MyInfoDto();
            myInfoDto.setResult(UNKNOWN);
            return myInfoDto;

        }

    }

    /**
     * 사용자 닉네임을 수정하는 내부 로직
     *
     * @param jwt          회원가입 및 로그인 시 발급되는 access token
     * @param userNickname 적용시킬 닉네임
     * @return 수정 성공 시 적용된 닉네임 반환, 성패에 따른 result 반환
     */
    public Map<String, String> editNicknameService(String jwt, String userNickname) {

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

            // 사용자의 닉네임을 변경한 후 DB에 기록
            UserEntity userEntity = userEntityOptional.get();
            userEntity.setUserNickname(userNickname);
            userRepository.saveAndFlush(userEntity);

            // 위의 과정을 무사히 통과했으므로
            serviceRes.put("result", SUCCESS);
            serviceRes.put("userNickname", userNickname);

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 프로필 사진을 수정하는 내부 로직
     *
     * @param jwt           회원가입 및 로그인 시 발급되는 access token
     * @param multipartFile 프로필 사진으로 사용할 jpg 이미지 파일
     * @return 성패에 따른 result 반환
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    public Map<String, String> editProfileService(String jwt, MultipartFile multipartFile) {

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

            // DB의 기존 프로필 사진 정보 조회
            UserEntity userEntity = userEntityOptional.get();
            String pastProfile = userEntity.getUserProfile();

            if (pastProfile != null) {
                // null 이 아니라는 것은 기존에 업로드한 이미지가 있다는 뜻
                // 서버의 해당 경로로 가서 파일 삭제
                Files.delete(Paths.get(pastProfile));
            }

            // 지난 버전의 프로필 사진이 서버에 남아있지 않음

            // UserSeq + 지금 시간 + 입력받은 파일명으로 서버에 저장
            long now = new Date().getTime();
            String fileName = userEntity.getUserId() + "_" + now + ".jpg";
            String filePath = BASE_URL + "userprofile/" + fileName;

            // 서버로 파일을 내보낼 output stream 열기
            // 파일을 읽을 input stream 열기

            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                 InputStream inputStream = multipartFile.getInputStream()) {

                // 한번에 읽어들인 글자의 수
                int readCount;

                // 한번에 읽을 만큼의 바이트를 지정
                // 1024, 2048 등의 크기가 일반적
                byte[] buffer = new byte[1024];

                // 파일을 모두 읽을 때까지 반복
                while ((readCount = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, readCount);
                }

            }

            // 저장된 프로필 사진의 경로를 DB에 기록
            userEntity.setUserProfile(filePath);
            userRepository.saveAndFlush(userEntity);

            // 위의 과정을 무사히 통과했으므로
            serviceRes.put("result", SUCCESS);

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);
        }

        return serviceRes;

    }

    /**
     * 프로필 사진을 삭제하는 내부 로직
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 성패에 따른 result 반환
     * @deprecated 현재 사용되고 있지 않으나, 이용 가능함
     */
    public Map<String, String> deleteProfileService(String jwt) {

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

            // DB의 기존 프로필 사진 정보 조회
            UserEntity userEntity = userEntityOptional.get();
            String pastProfile = userEntity.getUserProfile();

            if (pastProfile != null) {
                // null 이 아니라는 것은 기존에 업로드한 이미지가 있다는 뜻
                // 서버의 해당 경로로 가서 파일 삭제
                Files.delete(Paths.get(pastProfile));
            }

            // 지난 버전의 프로필 사진이 서버에 남아있지 않음

            // 프로필 사진이 삭제되었음을 DB에 기록
            userEntity.setUserProfile(null);
            userRepository.saveAndFlush(userEntity);

            // 위의 과정을 무사히 통과했으므로
            serviceRes.put("result", SUCCESS);

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 사용자의 Git 사용자명, 토큰을 수정하는 API
     *
     * @param jwt             회원가입 및 로그인 시 발급되는 access token
     * @param userGitUsername 사용자의 Git 사용자명
     * @param userGitToken    사용자의 Git 토큰
     * @return 수정 성공 시 적용된 Git 사용자명 반환, 성패에 따른 result 반환
     */
    public Map<String, String> editGitService(String jwt, String userGitUsername, String userGitToken) {

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

            // 사용자의 Git 정보를 변경한 후 DB에 기록
            UserEntity userEntity = userEntityOptional.get();
            userEntity.setUserGitUsername(userGitUsername);


            userEntity.setUserGitToken(userGitToken);
            userRepository.saveAndFlush(userEntity);

            // 위의 과정을 무사히 통과했으므로
            serviceRes.put("result", SUCCESS);
            serviceRes.put("userGitUsername", userGitUsername);

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;
    }

    /**
     * 비밀번호를 수정하는 내부 로직
     *
     * @param jwt             회원가입 및 로그인 시 발급되는 access token
     * @param userPassword    사용자의 기존 비밀번호
     * @param userNewPassword 적용시킬 비밀번호
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> editPasswordService(String jwt, String userPassword, String userNewPassword) {

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

            // DB에 기록된 패스워드 조회
            // 입력받은 패스워드와 일치하는지 확인
            UserEntity userEntity = userEntityOptional.get();
            String originPW = userEntity.getUserPassWord();
            String encodedPW = CryptoUtil.Sha256.hash(userPassword);

            if (originPW.equals(encodedPW)) {
                // 비밀번호 일치를 확인

                // userEntity의 비밀번호를 갱신하여 기록
                userEntity.setUserPassWord(CryptoUtil.Sha256.hash(userNewPassword));
                userRepository.saveAndFlush(userEntity);

                // 위의 과정을 무사히 통과했으므로
                serviceRes.put("result", SUCCESS);

            } else {
                // 비밀번호가 잘못됨
                serviceRes.put("result", WRONG);

            }

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 회원 탈퇴 내부 로직
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> quitService(String jwt) {

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

            // 사용자가 팀장인 모든 팀의 리스트
            List<TeamEntity> teamEntityList = teamRepository.findAllByTeamLeader_UserSeq(userSeq);
            // 사용자가 팀장인 단일팀의 리스트
            List<Long> teamSeqList = new ArrayList<>();

            // 사용자가 팀장인 팀이 있는지 확인함
            if (!teamEntityList.isEmpty()) {
                // 사용자가 팀장으로 있는 팀이 있다면,
                // 각 팀의 인원수를 파악함
                for (TeamEntity t : teamEntityList) {
                    // 다인팀의 팀장은 서비스를 탈퇴할 수 없음
                    // 따라서, 사용자가 팀장인 모든 팀이 단일팀이어야 탈퇴가 가능함

                    if (memberRepository.countAllByTeam_TeamSeq(t.getTeamSeq()) >= 2) {
                        // 단일팀이 아닌 팀이 하나라도 발견될 경우,
                        // 탐색을 중지하고 탈퇴 불가로 처리함
                        serviceRes.put("result", NO_PER);
                        return serviceRes;

                    } else {
                        // 단일팀일 경우, 추후 서버에서 삭제해야 하므로 해당 팀의 시퀀스를 기록함
                        teamSeqList.add(t.getTeamSeq());

                    }
                }
            }

            // 사용자가 팀장인 다인팀이 존재하지 않음, 탈퇴 가능
            // 사용자가 팀장인 단일팀을 서버에서 삭제해야 함

            if (!teamSeqList.isEmpty()) {
                Map<String, String> deleteProjectRes = projectService.deleteProjectService(teamSeqList);
                String result = deleteProjectRes.get("result");

                if (!result.equals(SUCCESS)) {
                    serviceRes.put("result", UNKNOWN);
                    return serviceRes;
                }

            }

            // 해당 사용자에 관련된 모든 팀 정보가 서버에서 삭제됨

            // 유저 테이블에서 사용자 삭제
            userRepository.delete(userEntity);

            // DB의 유저 테이블에서 사용자가 삭제됨에 따라,
            // 멤버 테이블에서 사용자의 모든 멤버 컬럼이 삭제됨
            // 팀 테이블에서 사용자가 팀장인 모든 팀 컬럼이 삭제됨

            // 위의 과정을 무사히 통과했으므로
            serviceRes.put("result", SUCCESS);

        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 유저별로 개인 환경 세팅을 저장하는 내부 로직
     *
     * @param jwt         회원가입 및 로그인 시 발급되는 access token
     * @param settingsDto 개인 환경 세팅 정보
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> personalPostService(String jwt, Long teamSeq, SettingsDto settingsDto) {

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

            // 입력받은 팀에 사용자가 멤버로 등록되어 있는지 확인
            Optional<MemberEntity> memberEntityOptional
                    = memberRepository.findByUser_UserSeqAndTeam_TeamSeq(userEntity.getUserSeq(), teamSeq);

            if (!memberEntityOptional.isPresent()) {
                // 해당 팀에 속해있지 않음
                serviceRes.put("result", NO_PER);
                return serviceRes;
            }

            MemberEntity memberEntity = memberEntityOptional.get();

            // DB에 varchar 형태로 저장하기 위해 JSON을 String꼴로 치환함
            JSONObject jsonObject = new JSONObject(settingsDto);
            String settings = jsonObject.toString();

            memberEntity.setSettings(settings);
            memberRepository.saveAndFlush(memberEntity);

            // 위의 과정을 무사히 통과했으므로
            serviceRes.put("result", SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            serviceRes.put("result", UNKNOWN);

        }

        return serviceRes;

    }

    /**
     * 사용자별 개인 환경 세팅을 조회하는 내부 로직
     *
     * @param jwt 회원가입 및 로그인 시 발급되는 access token
     * @return 조회 성공 시 개인 환경 세팅 정보 반환, 성패에 따른 result 반환
     */
    public SettingsDto personalGetService(String jwt, Long teamSeq) {

        try {

            // jwt가 인증하는 사용자의 UserEntity를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(jwtService.JWTtoUserSeq(jwt));

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                SettingsDto settingsDto = new SettingsDto();
                settingsDto.setResult(NO_SUCH);
                return settingsDto;
            }

            UserEntity userEntity = userEntityOptional.get();

            // 입력받은 팀에 사용자가 멤버로 등록되어 있는지 확인
            Optional<MemberEntity> memberEntityOptional
                    = memberRepository.findByUser_UserSeqAndTeam_TeamSeq(userEntity.getUserSeq(), teamSeq);

            if (!memberEntityOptional.isPresent()) {
                // 해당 팀에 속해있지 않음
                SettingsDto settingsDto = new SettingsDto();
                settingsDto.setResult(NO_PER);
                return settingsDto;
            }

            MemberEntity memberEntity = memberEntityOptional.get();
            String settings = memberEntity.getSettings();

            if(settings.isEmpty()){
                // 만약 기존에 저장되어 있는 내용이 없으므로
                SettingsDto settingsDto = new SettingsDto();
                settingsDto.setResult("NO_VALUE");
                return settingsDto;
            }

            // 내보내기 위한 SettingsDto 생성
            ObjectMapper mapper = new ObjectMapper();
            SettingsDto settingsDto = mapper.readValue(settings, SettingsDto.class);

            // 위의 과정을 무사히 통과했으므로
            settingsDto.setResult(SUCCESS);
            return settingsDto;

        } catch (Exception e) {
            SettingsDto settingsDto = new SettingsDto();
            settingsDto.setResult(UNKNOWN);
            return settingsDto;

        }

    }

    /**
     * 사용자의 프로필을 UserSeq로 조회하는 내부 로직
     *
     * @param userSeq 프로필을 조회하고자 하는 사용자의 UserSeq
     * @return 조회 성공 시 외부인이 접근 가능한 사용자 정보 반환, 성패에 따른 result 반환
     */
    public UserInfoDto mypageService(Long userSeq) {

        try {

            // 입력받은 UserSeq를 가지는 사용자를 추출
            Optional<UserEntity> userEntityOptional = userRepository.findById(userSeq);

            // 해당하는 사용자가 존재하는지 확인
            if (!userEntityOptional.isPresent()) {
                // 해당하는 사용자가 없음
                UserInfoDto userInfoDto = new UserInfoDto();
                userInfoDto.setResult(NO_SUCH);
                return userInfoDto;
            }

            UserEntity userEntity = userEntityOptional.get();

            // 내보내기 위한 UserInfoDto 생성
            UserInfoDto userInfoDto = new UserInfoDto(userEntity);

            // 위의 과정을 무사히 통과했으므로
            userInfoDto.setResult(SUCCESS);
            return userInfoDto;

        } catch (Exception e) {
            UserInfoDto userInfoDto = new UserInfoDto();
            userInfoDto.setResult(UNKNOWN);
            return userInfoDto;

        }

    }

    /**
     * 사용자를 검색하는 내부 로직
     *
     * @param searchWord 검색하고자 하는 단어
     * @return 조회 성공 시 searchWord가 Id 혹은 Nickname에 포함되는 사용자의 리스트를 반환
     */
    public List<UserInfoDto> searchUser(String searchWord) {

        try {
            List<UserEntity> userEntityList =
                    userRepository.findAllByUserIdContainingOrUserNicknameContaining(searchWord, searchWord);

            // 내보내기 위한 List<UserInfoDto> 생성
            List<UserInfoDto> userInfoDtoList = new ArrayList<>();
            for (UserEntity u : userEntityList) {
                UserInfoDto userInfoDto = new UserInfoDto(u);
                userInfoDto.setResult(SUCCESS);
                userInfoDtoList.add(userInfoDto);
            }

            return userInfoDtoList;

        } catch (Exception e) {
            return null;

        }
    }
}
