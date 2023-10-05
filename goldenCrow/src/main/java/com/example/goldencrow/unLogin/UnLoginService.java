package com.example.goldencrow.unLogin;

import com.example.goldencrow.compile.CompileService;
import com.example.goldencrow.file.service.FileService;
import com.example.goldencrow.file.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

@Service
public class UnLoginService {

    @Autowired
    private CompileService compileService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UnLoginRepository unLoginRepository;

    public String getSessionId() {
        return "hello";
    }

    public Map<String, String> unloginCompileService(String sessionId, String fileContent, String input) {
        Map<String, String> serviceRes = new HashMap<>();
        System.out.println("sessionId : " + sessionId);
        String SESSION_PATH = "/home/ubuntu/crow_data/UnLoginUser/" + sessionId;

        // 세션 디렉토리 생성
        String dirCreated = projectService.createDirService(BASE_URL + "UnLoginUser/", sessionId);
        if (dirCreated.equals(DUPLICATE)) {
            serviceRes.put("result", DUPLICATE);
            return serviceRes;
        }

        String filePath = SESSION_PATH + "/" + sessionId + ".py";

        // 파일 내용 임시 저장 (/home/ubuntu/crow_data/UnLoginUser/{sessionId}/{sessionId}.py)
        // 임시 파일 생성
        File file = new File(filePath);

        // 임시파일에 content 저장
        try (FileWriter overWriteFile = new FileWriter(file, false)) {
            overWriteFile.write(fileContent);
        } catch (IOException e) {
            System.out.println("파일 내용 저장 에서 터짐");
            serviceRes.put("result", UNKNOWN);
            return serviceRes;
        }

        String content;
        if (input.isEmpty()) {
            content = "FROM python:3.10\n" +
                    "CMD [\"python3\", \"" + filePath + "\"]\n" +
                    "EXPOSE 3000";
        } else {
            String inputString = "\"" + input + "\"";
            content = "FROM python:3.10\n" +
                    "CMD /bin/sh \n" +
                    "CMD echo " + inputString +
                    " | python3 " + filePath + "\n" +
                    "EXPOSE 3000";
        }

        // Dockerfile 생성
        File dockerFile = new File(SESSION_PATH + "/Dockerfile");

        // Dockerfile에 content 저장
        try (FileWriter overWriteFile = new FileWriter(dockerFile, false)) {
            overWriteFile.write(content);
        } catch (IOException e) {
            System.out.println("도커파일 생성에서 터짐");
            serviceRes.put("result", UNKNOWN);
            return serviceRes;
        }

        // 컨테이너명
        String conAndImgName = "unlogin_" + sessionId.toLowerCase();
        System.out.println("unlogin conAndImgName: " + conAndImgName);
        String[] imgCmd = {"docker", "build", "-t", conAndImgName, SESSION_PATH + "/"};
        // docker image build
        String imageBuild = compileService.resultStringService(imgCmd);
        if (imageBuild.isEmpty()) {
            serviceRes.put("result", UNKNOWN);
            return serviceRes;
        }
        // docker container run
        String[] containerCmd = {"docker", "run", "-d", "--name", conAndImgName, "-v",
                SESSION_PATH + ":" + SESSION_PATH, "-P", conAndImgName};
        String containerRun = compileService.resultStringService(containerCmd);
        // 출력값 반환
        String[] pythonCmd = {"docker", "logs", "-f", conAndImgName};
        String pythonResponse = compileService.resultStringService(pythonCmd);
        String pathUpdateRes = pythonResponse.replace(BASE_URL + "UnLoginUser", "");
        System.out.println(pythonResponse);
        serviceRes.put("result", SUCCESS);
        serviceRes.put("response", pathUpdateRes);

        // 도커 컨테이너 멈추기
        String[] containerStop = {"docker", "stop", conAndImgName};
        String stopedCon = compileService.resultStringService(containerStop);
        System.out.println("컨테이너 중지" + stopedCon);

        // 컨테이너 삭제
        String[] containerRm = {"docker", "rm", conAndImgName};
        String removedCon = compileService.resultStringService(containerRm);
        System.out.println("컨테이너 삭제" + removedCon);

        // 도커 이미지 삭제
        String[] imageRm = {"docker", "rmi", conAndImgName};
        String rmImg = compileService.resultStringService(imageRm);
        System.out.println("이미지 삭제" + rmImg);

        // 도커파일 삭제
        fileService.serverFileDeleteService(2, SESSION_PATH + "/Dockerfile");
        // 세션 디렉토리 삭제
        fileService.serverFileDeleteService(1, SESSION_PATH);

        return serviceRes;

    }

}
