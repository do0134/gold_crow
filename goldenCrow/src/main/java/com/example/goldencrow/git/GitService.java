package com.example.goldencrow.git;

import com.example.goldencrow.file.FileEntity;
import com.example.goldencrow.file.FileRepository;
import com.example.goldencrow.file.service.ProjectService;
import com.example.goldencrow.team.entity.TeamEntity;
import com.example.goldencrow.team.repository.TeamRepository;
import com.example.goldencrow.user.UserEntity;

import com.example.goldencrow.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

import static com.example.goldencrow.common.Constants.*;

/**
 * git과 관련된 로직을 처리하는 service
 */
@Service
public class GitService {

    private final ProjectService projectService;

    private ProcessBuilder command;
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;


    /**
     * Git Service 생성자
     *
     * @param projectService project를 관리하는 service
     * @param userRepository user Table에 접속하는 Repository
     */
    public GitService(ProjectService projectService, UserRepository userRepository, FileRepository fileRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    /**
     * Git 정보 불러오는 Service
     *
     * @param userSeq 불러오려는 Git의 사용자 Sequence
     * @return gitInfo 반환, 없을 시 NO_SUCH 반환
     */
    public List<String> getGitInfoService(Long userSeq) {
        List<String> gitInfo = new ArrayList<>();
        Optional<UserEntity> teamLeader = userRepository.findByUserSeq(userSeq);
        if (!teamLeader.isPresent()) {
            gitInfo.add(NO_SUCH);
            return gitInfo;
        }
        String email = teamLeader.get().getUserGitUsername();
        String token = teamLeader.get().getUserGitToken();

        // 사용자의 git 정보가 없는 경우
        if (email == null || token == null) {
            gitInfo.add(NO_SUCH);
            return gitInfo;
        }
        gitInfo.add(email);
        gitInfo.add(token);
        return gitInfo;
    }

    /**
     * gitPath 만들어주는 함수
     *
     * @param teamSeq gitPath를 만들 팀의 Sequence
     * @return gitPath
     */
    public String getGitPath(Long teamSeq) {
        Optional<List<FileEntity>> files = fileRepository.findAllByTeamSeq(teamSeq);
        if (!files.isPresent()) {
            return NO_SUCH;
        }
        List<FileEntity> teamFiles = files.get();

        for (FileEntity teamFile : teamFiles) {
            if (teamFile.getFileTitle().equals(".git")) {
                String gitPath = teamFile.getFilePath().replace("/.git", "");
                return gitPath;
            }
        }
        return NO_SUCH;
    }

    /**
     * git clone 내부 로직
     * 팀, 프로젝트 디렉토리를 순차적으로 생성(프로젝트 서비스 함수 이용)
     * 그 후 프로젝트 디렉토리에서 클론
     * 클론한 디렉토리로 이동 후 유저 정보 입력(리더 이메일, 리더 닉네임)
     *
     * @param url         clone받을 git 주소
     * @param teamSeq     해당 프로젝트의 팀 sequence
     * @param projectName 해당 프로젝트명
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> gitCloneService(String url, Long teamSeq, String projectName) {

        Map<String, String> serviceRes = new HashMap<>();

        // 팀시퀀스로 팀이 존재하는지 확인
        Optional<TeamEntity> thisTeam = teamRepository.findByTeamSeq(teamSeq);
        if (!thisTeam.isPresent()) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        TeamEntity team = thisTeam.get();
        List<String> gitInfoCheck = getGitInfoService(team.getTeamLeader().getUserSeq());
        // 팀 리더의 깃 정보가 존재하는지 확인하는 로직
        if (gitInfoCheck.get(0).equals(NO_SUCH)) {
            serviceRes.put("result", NO_PER);
            return serviceRes;
        }

        // 명령어 실행 시키기 위한 ProcessBuilder
        ProcessBuilder command = new ProcessBuilder("git", "clone", url);

        // 팀 시퀀스 디렉토리 만들기
        String teamFolder = String.valueOf(teamSeq);
        String newFilePath = projectService.createDirService(BASE_URL, teamFolder);

        if (newFilePath.equals("2")) {
            serviceRes.put("result", WRONG);
            return serviceRes;
        }

        // 프로젝트 이름 디렉토리 만들기
        String pjt = projectService.createDirService(newFilePath + "/", projectName);

        if (pjt.equals("2")) {
            serviceRes.put("result", WRONG);
            return serviceRes;
        }
        File newProjectFolder = new File(pjt);

        // 프로젝트 디렉토리에서 명령어 실행
        command.directory(new File(pjt));
        try {
            command.start().waitFor();
        } catch (IOException e) {
            serviceRes.put("result", WRONG);
            return serviceRes;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            serviceRes.put("result", WRONG);
            return serviceRes;
        }

        // 클론을 받아왔지만 아무런 파일과 폴더가 없는 경우 실패처리
        if (newProjectFolder.listFiles() == null
                || Objects.requireNonNull(newProjectFolder.listFiles()).length == 0) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }
        File newFolder = Objects.requireNonNull(newProjectFolder.listFiles())[0];


        // config 파일 세팅
        String configResult = setConfigService(newFolder, team);
        if (!configResult.equals(SUCCESS)) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        projectService.saveFilesInDIrService(pjt + "/", teamSeq);
        serviceRes.put("result", SUCCESS);
        return serviceRes;


    }

    /**
     * 팀에서 리더의 닉네임, 이메일을 받아 git config에 등록하는 내부 로직
     * 팀 Entity와 파일을 입력받음
     *
     * @param file config를 등록할 파일 정보
     * @param team config에 등록할 팀 정보
     * @return 성패에 따른 String
     */
    public String setConfigService(File file, TeamEntity team) {
        UserEntity leader = team.getTeamLeader();

        String leaderEmail = leader.getUserId();
        String leaderName = leader.getUserNickname();

        // git config에 등록하기 위해 ProcessBuilder 사용
        ProcessBuilder configEmail = new ProcessBuilder("git", "config", "user.email", leaderEmail);
        ProcessBuilder configName = new ProcessBuilder("git", "config", "user.name", leaderName);

        configEmail.directory(file);
        configName.directory(file);

        try {
            configEmail.start();
            configName.start();
        } catch (IOException e) {
            return e.getMessage();
        }
        return SUCCESS;
    }

    /**
     * Git Switch를 처리하는 내부 로직
     *
     * @param teamSeq    팀의 시퀀스
     * @param branchName switch할 brnach의 이름
     * @param type       switch할 branch의 종류 (1 : 존재하는 브랜치로 이동, 2 : 브랜치를 새로 생성 후 이동)
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> gitSwitchService(String branchName, Integer type, Long teamSeq) {
        Map<String, String> serviceRes = new HashMap<>();
        String gitPath = getGitPath(teamSeq);

        File targetFile = new File(gitPath);

        serviceRes.put("message", "");

        ProcessBuilder command = new ProcessBuilder();
        // switch할 branch의 종류로 명령어 저장
        if (type == 1) {
            command.command("git", "switch", branchName);
        } else {
            command.command("git", "switch", "-c", branchName);
        }
        // 명령어를 실행할 파일 설정
        command.directory(targetFile);
        // 명령어 수행 후 결과값을 저장하기 위한 StringBuilder
        StringBuilder msg = new StringBuilder();

        // 명령어 수행 로직
        try {
            String result;
            Process p = command.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((result = br.readLine()) != null) {
                msg.append(result).append("\n");
            }
        } catch (IOException e) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        String message = msg.toString();
        serviceRes.put("result", SUCCESS);
        // 성공 여부 판단
        serviceRes.put("message", message);
        return serviceRes;
    }

    /**
     * Git add를 처리하는 내부 로직
     *
     * @param gitPath  명령어를 수행할 디렉토리
     * @param filePath add할 파일 경로 (특정하지 않으면 all)
     * @return 성패에 따른 String 반환
     */
    public Map<String, String> gitAddService(String gitPath, String filePath) {
        ProcessBuilder command = new ProcessBuilder();
        Map<String, String> serviceRes = new HashMap<>();
        serviceRes.put("addMessage", "");
        // filePath를 입력했으면 filePath 사용 / add할 파일을 특정하지 않았으면 "."
        if (filePath.equals("all")) {
            command.command("git", "add", ".");
        } else {
            command.command("git", "add", filePath);
        }
        // 명령어를 수행할 path 등록
        command.directory(new File(gitPath));

        // 명령어 수행 후 결과값을 저장하기 위한 StringBuilder
        StringBuilder msg = new StringBuilder();

        // 명령어 수행 로직
        try {
            Process p = command.start();
            String forPrint;
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((forPrint = br.readLine()) != null) {
                msg.append(forPrint);
                msg.append("\n");
            }
            p.waitFor();
            serviceRes.put("addMessage", msg.toString());
        } catch (IOException e) {
            serviceRes.put("result",NO_SUCH);
            return serviceRes;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            serviceRes.put("result",UNKNOWN);
            return serviceRes;
        }
        // 성공 여부 판단
        if (msg.length() == 0) {
            serviceRes.put("result",SUCCESS);
            return serviceRes;
        }
        serviceRes.put("result",UNKNOWN);
        return serviceRes;
    }

    /**
     * Git commit을 처리하는 내부 로직
     * Git add 후 성공한다면 Git commit
     *
     * @param message  commit message
     * @param teamSeq  커밋할 팀 Seq
     * @param filePath add할 파일 경로 (특정하지 않으면 all)
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> gitCommitService(String message, Long teamSeq, String filePath) {
        Map<String, String> serviceRes = new HashMap<>();
        serviceRes.put("commitMessage", "");

        String gitPath = getGitPath(teamSeq);
        // git add 로직 수행
        Map<String, String> gitAddCheck = gitAddService(gitPath, filePath);
        serviceRes.put("addMessage",gitAddCheck.get("addMessage"));
        if (!gitAddCheck.get("result").equals(SUCCESS)) {
            serviceRes.put("result", gitAddCheck.get("result"));
            return serviceRes;
        }

        // git commit을 수행할 명령어
        ProcessBuilder command = new ProcessBuilder("git", "commit", "-m", message);
        // 명령어를 수행할 path 등록
        command.directory(new File(gitPath));

        // 명령어 수행 후 결과값을 저장하기 위한 StringBuilder
        StringBuilder msg = new StringBuilder();
        msg.append("Success");
        msg.append("\n");

        // 명령어 수행 로직
        try {
            Process p = command.start();
            String forPrint;
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((forPrint = br.readLine()) != null) {
                msg.append(forPrint);
                msg.append("\n");
            }
            serviceRes.put("commitMessage", msg.toString());
            p.waitFor();
        } catch (IOException e) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            serviceRes.put("result", UNKNOWN);
            return serviceRes;
        }

        // 성공 여부 판단
        serviceRes.put("result", SUCCESS);

        return serviceRes;
    }

    /**
     * Git push를 처리하는 내부 로직
     * Git commit 후 성공한다면 push를 수행
     *
     * @param branchName push할 branch의 이름
     * @param message    commit message
     * @param teamSeq    commit할 팀 Seq
     * @param filePath   push할 파일의 경로 (특정하지 않을 경우 all)
     * @param userSeq    push하는 사용자의 Sequence
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> gitPushService(String branchName, String message, Long teamSeq, String filePath, Long userSeq) {
        Map<String, String> serviceRes = new HashMap<>();
        String gitPath = getGitPath(teamSeq);
        serviceRes.put("pushMessage", "");
        // commit 로직 수행
        Map<String, String> gitCommitCheck = gitCommitService(message, teamSeq, filePath);
        serviceRes.put("addMessage", gitCommitCheck.get("addMessage"));
        serviceRes.put("commitMessage",gitCommitCheck.get("commitMessage"));

        if (!gitCommitCheck.get("result").equals(SUCCESS)) {
            serviceRes.put("result", gitCommitCheck.get("result"));
            return serviceRes;
        }

        // push하기 위해 remote URL 가져오는 로직 수행
        String gitUrl = getRemoteUrlService(gitPath);
        if (!gitUrl.contains("https")) {
            serviceRes.put("result", gitUrl);
            return serviceRes;
        }

        // git 정보 불러오기 (email, password)
        List<String> gitInfo = getGitInfoService(userSeq);
        // git 정보를 불러오지 못한 경우
        if (gitInfo.size() < 2) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        String email = gitInfo.get(0);
        String pass = gitInfo.get(1);

        String newGitUrl = newRemoteUrlService(gitUrl, email, pass);

        boolean setNew = setNewUrlService(newGitUrl, gitPath);

        if (!setNew) {
            serviceRes.put("result", WRONG);
            return serviceRes;
        }

        // Git Push 명령어
        ProcessBuilder command = new ProcessBuilder("git", "push", "origin", branchName);
        command.redirectErrorStream(true);
        // 명령어를 수행할 프로젝트 경로 설정
        command.directory(new File(gitPath));
        // 명령어 수행 후 결과값을 저장하기 위한 StringBuilder
        StringBuilder msg = new StringBuilder();
        // 명령어 수행 로직
        try {
            String read;
            Process p = command.start();
            BufferedReader result = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((read = result.readLine()) != null) {
                msg.append(read).append("\n");
            }
            p.waitFor();
        } catch (IOException e) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            serviceRes.put("result",UNKNOWN);
            return serviceRes;
        }

        // 경로 재설정 로직 수행
        boolean returnOld = setNewUrlService(gitUrl, gitPath);


        // 경로 재설정에 실패한 경우
        if (!returnOld) {
            serviceRes.put("result", UNKNOWN);
            return serviceRes;
        }
        serviceRes.put("pushMessage",msg.toString());

        serviceRes.put("result", SUCCESS);
        return serviceRes;
    }

    /**
     * Branch 목록을 조회하는 내부 로직
     *
     * @param teamSeq 브랜치 조회하려는 팀 Seq
     * @param type    조회하려는 branch의 종류 (1 : local branch, 2 : remote branch)
     * @return branch 목록을 List<String>으로 반환, 없거나 오류가 날 경우 null
     */
    public List<String> getBranchService(Long teamSeq, int type) {
        List<String> branches = new ArrayList<>();
        ProcessBuilder command = new ProcessBuilder();

        String gitPath = getGitPath(teamSeq);

        // 조회하려는 브랜치에 따라 명령어 저장
        if (type == 1) {
            command.command("git", "branch");
        } else if (type == 2) {
            command.command("git", "branch", "-r");
        } else {
            return null;
        }

        // 명령어를 수행할 프로젝트의 경로 저장
        command.directory(new File(gitPath));
        String read;

        // 명령어 수행 로직
        try {
            Process getBranch = command.start();
            BufferedReader branch = new BufferedReader(new InputStreamReader(getBranch.getInputStream()));
            while ((read = branch.readLine()) != null) {
                branches.add(read.trim());
            }
            getBranch.waitFor();
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return branches;
    }

    /**
     * 현재 연결된 깃의 URL을 받아오는 내부 로직
     *
     * @param gitPath 로직을 수행할 프로젝트의 경로
     * @return 성공 시 remoteURL를 반환, 실패 시 result 반환
     */
    public String getRemoteUrlService(String gitPath) {
        // 현재 Git을 관리하는 Url을 받아오는 리눅스 명령어
        command = new ProcessBuilder("git", "remote", "-vv");
        command.directory(new File(gitPath));
        String returnUrl = null;

        try {
            // 리눅스 명령어 결과값을 받아오는 로직
            Process p = command.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String reader = null;

            while ((reader = br.readLine()) != null) {
                returnUrl = reader;
            }
        } catch (IOException e) {
            return NO_SUCH;
        }

        if (returnUrl == null) {
            return UNKNOWN;
        } else {
            // 받아온 결과값에서 순수 Url부분만 보낼 수 있도록 정제하는 로직
            returnUrl = returnUrl.replace("origin", "");
            returnUrl = returnUrl.replace("(push)", "");
            returnUrl = returnUrl.trim();

            return returnUrl;
        }

    }

    /**
     * 푸쉬를 하기 위한 새로운 Url을 만들어주는 함수(만들기만 하고 세팅하진 않음!)
     *
     * @param basicPath 기존 Git Info Url
     * @param email     유저의 Git Id
     * @param pass      유저의 Git token
     * @return newUrl Git push /pull 할 새로운 Git Info Url
     */
    public String newRemoteUrlService(String basicPath, String email, String pass) {
        String id;
        StringBuffer sb = new StringBuffer();
        // @를 포함한 특수문자는 들어갈 수 없으므로, 이메일 형식에서 아이디만 빼온다. 
        for (int i = 0; i < email.length(); i++) {
            if (String.valueOf(email.charAt(i)).equals("@")) {
                break;
            }
            sb.append(String.valueOf(email.charAt(i)));
        }
        id = sb.toString();

        // 기존 Url을 개인정보 양식에 맞게 바꿔서 return 함
        String returnPath = basicPath.replace("https://", String.format("https://%s:%s@", id, pass));
        return returnPath;
    }

    /**
     * push / pull할 새로운 URL 세팅 처리하는 내부 로직
     *
     * @param newUrl  새로운 URL
     * @param gitPath git 로직을 사용할 프로젝트 경로
     * @return 성패에 따른 boolean값 반환
     */
    public Boolean setNewUrlService(String newUrl, String gitPath) {
        // 받은 Url로  Git Info Url을 변경
        command.command("git", "remote", "set-url", "origin", newUrl);
        command.directory(new File(gitPath));

        try {
            command.start();
        } catch (IOException e) {

            return false;
        }
        return true;
    }

    /**
     * 새로운 push/pull 할 수 있는 git Url을 설정해주는 통합 관리 내부 로직
     *
     * @param gitPath push/pull을 수행할 프로젝트 경로
     * @param email   사용자의 email 정보
     * @param pass    사용자의 password 정보
     * @return 성패에 따른 result String 반환
     */
    public String setUrlService(String gitPath, String email, String pass) {
        // 기존 Git Info Url을 받아옴
        String gitUrl = getRemoteUrlService(gitPath);
        if (!gitUrl.contains("https")) {
            return gitUrl;
        }
        // 받아온 Git Info Url을 개인 Id에 맞게 변환시킴
        String newRemoteUrl = newRemoteUrlService(gitUrl, email, pass);
        // 변환된 Url을 Git Info에 세팅함
        Boolean check = setNewUrlService(newRemoteUrl, gitPath);
        if (!check) {
            return UNKNOWN;
        }
        return SUCCESS;
    }

    /**
     * 바뀌었던 깃 Url을 기존 상태로 원위치 처리하는 내부 로직
     *
     * @param oldUrl  원래 팀 Git Info Url
     * @param gitPath 깃 명령어가 실행될 디렉토리
     * @return 성패에 따른 result String 반환
     */
    public String reUrlService(String oldUrl, String gitPath) {
        // 원래 Url을 받아서 바뀌었던 깃 Url을 원래 Url로 세팅
        Boolean check = setNewUrlService(oldUrl, gitPath);
        if (!check) {
            return UNKNOWN;
        }
        return SUCCESS;
    }

    /**
     * Git pull을 처리하는 내부 로직
     *
     * @param teamSeq   pull 실행할 team Seq
     * @param userSeq   pull받을 사용자의 sequence
     * @param brachName pull받을 branch의 이름
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> gitPullService(Long teamSeq, Long userSeq, String brachName) {
        Map<String, String> serviceRes = new HashMap<>();
        String gitPath = getGitPath(teamSeq);
        // pull받을 remote URL 조회 로직 수행
        String gitUrl = getRemoteUrlService(gitPath);
        // git 정보 조회 로직 수행
        List<String> gitInfo = getGitInfoService(userSeq);
        // git 정보를 조회할 수 없는 경우
        if (gitInfo.size() < 2) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        String email = gitInfo.get(0);
        String pass = gitInfo.get(1);

        // Git Push/Pull을 하기 위한 Url 세팅 성공 여부 확인
        String check = setUrlService(gitPath, email, pass);
        if (!check.equals(SUCCESS)) {
            serviceRes.put("result", check);
            return serviceRes;
        }

        // pull을 수행하는 명령어
        ProcessBuilder pb = new ProcessBuilder("git", "pull", "origin", brachName);
        // 명령어를 수행할 path 등록
        pb.directory(new File(gitPath));
        // 명령어 수행 후 결과값을 저장하기 위한 StringBuilder
        StringBuilder msg = new StringBuilder();
        // 명령어 수행 로직
        try {
            String result;
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((result = br.readLine()) != null) {
                msg.append(result);
                msg.append("\n");
            }
        } catch (IOException e) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        String result = reUrlService(gitUrl, gitPath);

        if (msg.length() == 0) {
            serviceRes.put("result", SUCCESS);
        } else {
            serviceRes.put("result", UNKNOWN);
        }
        return serviceRes;
    }
}