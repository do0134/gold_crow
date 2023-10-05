package com.example.goldencrow.compile;

import com.example.goldencrow.file.dto.FileCreateDto;
import com.example.goldencrow.file.service.FileService;
import com.example.goldencrow.file.service.ProjectService;
import com.example.goldencrow.team.entity.TeamEntity;
import com.example.goldencrow.team.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.example.goldencrow.common.Constants.*;

/**
 * 컴파일과 관련된 로직을 처리하는 Service
 */
@Service
public class CompileService {
    private String FLASK = "import Flask";
    private String FASTAPI = "import FastAPI";
    private String DJANGO = "os.environ.setdefault('DJANGO_SETTINGS_MODULE'";

    @Autowired
    private FileService fileService;
    @Autowired
    private ProjectService projectService;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * 명령어를 linux bash에 입력, 출력된 내용을 String으로 반환하는 내부 로직
     *
     * @param cmd 명령어
     * @return 명령어 수행 성공 시 결과 문자열 반환, 성패에 따른 result 반환
     */
    public String resultStringService(String[] cmd) {
        System.out.println(Arrays.toString(cmd));
        ProcessBuilder command = new ProcessBuilder(cmd);
        command.redirectErrorStream(true);
        StringBuilder msg = new StringBuilder();

        try {
            String read;
            Process p = command.start();
            BufferedReader result = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((read = result.readLine()) != null) {
                msg.append(read).append("\n");
            }
            p.waitFor();
        } catch (IOException e) {
            return NO_SUCH;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return UNKNOWN;
        }
        return msg.toString();
    }

    /**
     * 각 프로젝트 종류에 따라 도커파일을 생성하는 내부 로직
     *
     * @param absolutePath 도커파일을 생성하려는 절대 경로 (/home/ubuntu/crow_data/teamSeq/teamName/main.py)
     * @param teamSeq      도커파일을 생성하려는 프로젝트의 팀 sequence
     * @param type         프로젝트의 타입 번호 (1: pure python, 2: django, 3: flask, 4: fastapi)
     * @return 성패에 따른 result 반환
     */
    public String createDockerfile(String absolutePath, Long teamSeq, int type, String input) {
        // teamSeq가 DB에 존재하는지 체크
        Optional<TeamEntity> existTeam = teamRepository.findByTeamSeq(teamSeq);
        if (!existTeam.isPresent()) {
            return NO_SUCH;
        }
        System.out.println("createDockerfile에서 absolutePath : " + absolutePath);
        String[] pathList = absolutePath.split("/");
        System.out.println(Arrays.toString(pathList));
        int lastIdx = pathList.length - 1;
        System.out.println("lastIdx" + lastIdx);
//        String teamSeq = pathList[4];
        String teamSeqPath = pathList[0] + "/" + pathList[1] + "/" + pathList[2] + "/"
                + pathList[3] + "/" + pathList[4];
        System.out.println("teamSeqPath" + teamSeqPath);
        System.out.println(pathList[lastIdx]);
        String[] mainFileNameList = pathList[lastIdx].split("\\.");
        System.out.println("mainFileNameList " + Arrays.toString(mainFileNameList));
        String mainFileName = mainFileNameList[0];
        System.out.println("mainFileName" + mainFileName);
        StringBuilder filePathBuilder = new StringBuilder();
        for (int i = 5; i <= lastIdx; i++) {
            filePathBuilder.append(pathList[i]).append("/");
        }
        String filePath = filePathBuilder.substring(0, filePathBuilder.length() - 1);

        String fastapiMainPath = absolutePath.replace(mainFileName + ".py", "");

        String content = "";

        // 1: pure Python, 2 : Django, 3 : Flask, 4 : FastAPI
        if (type == 1) {
            if (input.isEmpty()) {
                content = "FROM python:3.10\n" +
                        "CMD [\"python3\", \"" + absolutePath + "\"]\n" +
                        "EXPOSE 3000";
            } else {
                String inputString = "\"" + input + "\"";
                content = "FROM python:3.10\n" +
                        "CMD /bin/sh \n" +
                        "CMD echo " + inputString +
                        " | python3 " + absolutePath + "\n" +
                        "EXPOSE 3000";
            }
        } else if (type == 2) {
            content = "FROM python:3.10\n" +
                    "RUN pip3 install django\n" +
                    "WORKDIR " + teamSeqPath + "\n" +
                    "COPY . .\n" +
                    "CMD [\"python3\", \"" + filePath + "\", \"runserver\", \"0.0.0.0:3000\"]\n" +
                    "EXPOSE 3000";
        } else if (type == 3) {
            content = "FROM python:3.10\n" +
                    "WORKDIR " + teamSeqPath + "\n" +
                    "COPY . .\n" +
                    "RUN pip3 install Flask\n" +
                    "EXPOSE 5000\n" +
                    "CMD [ \"python3\" , \"" + filePath + "\", \"run\", \"--host=0.0.0.0\"]";
        } else if (type == 4) {
            content = "FROM python:3.10\n" +
                    "WORKDIR " + fastapiMainPath + "\n" +
                    "RUN python3 -m venv venv\n" +
                    "RUN . ./venv/bin/activate\n" +
                    "RUN pip3 install uvicorn[standard]\n" +
                    "RUN pip3 install fastapi\n" +
                    "COPY . .\n" +
                    "EXPOSE 8000\n" +
                    "CMD [\"uvicorn\", \"" + mainFileName +
                    ":app" + "\", \"--host\", \"0.0.0.0\"]";
        }

        // Dockerfile 생성
        File file = new File(teamSeqPath + "/Dockerfile");

        // Dockerfile에 content 저장
        try (FileWriter overWriteFile = new FileWriter(file, false)) {
            overWriteFile.write(content);
        } catch (IOException e) {
            return UNKNOWN;
        }
        // DB에 저장
        FileCreateDto fileCreateDto;
        fileCreateDto =
                new FileCreateDto("Dockerfile", teamSeqPath + "/Dockerfile", teamSeq);
        fileService.insertFileService(fileCreateDto);
        return SUCCESS;
    }

    /**
     * 컨테이너 Id로 사용중인 포트 번호를 찾는 내부 로직
     *
     * @param container 포트를 찾으려는 컨테이너 Id
     * @return 명령어를 수행하고 나온 출력값 반환
     */
    public String portNumService(String container) {
        // linux bash에서 포트 번호를 검색하는 명령어
        String[] command = {"docker", "port", container};

        // 명령어 실행 로직 수행
        String result = resultStringService(command);
        System.out.println(result);
        if (result.startsWith("Error: No such container")) {
            return NO_SUCH;
        }
        // \n 전까지의 문자열에서 : 뒤에 있는 숫자만 가져오기
        String[] portList = result.split("\n");
        String[] containerPort = portList[0].split(":");
        System.out.println(Arrays.toString(containerPort));
        // 서버 URL 생성
        return containerPort[1];
    }

    /**
     * 프로젝트 혹은 파일을 컴파일하는 내부 로직
     *
     * @param type     프로젝트의 타입 (1: pure Python, 2: Django, 3: Flask, 4: FastAPI)
     * @param filePath 컴파일을 수행할 프로젝트 혹은 파일의 절대경로 (/home/ubuntu/crow_data/teamSeq/teamName/main.py)
     * @param input    pure python 파일일 때 input값 (없으면 빈 문자열)
     * @return 컴파일 성공 시 컴파일 결과 반환, 성패에 따른 result 반환
     */
    public Map<String, String> pyCompileService(int type, String filePath, String input) {
        Map<String, String> serviceRes = new HashMap<>();
        String[] pathList = filePath.split("/");
        int lastIdx = pathList.length - 1;
        String teamSeq = pathList[4];
        String teamName = pathList[5];
        // 프로젝트명과 teamSeq로 docker container와 image 이름 생성
        String conAndImgName = "crowstudio_" + teamName.toLowerCase().replaceAll(" ", "") + "_" + teamSeq;
        // 현재 실행되고 있는 컨테이너, 이미지 삭제, 도커파일 삭제
        Map<String, String> stopped = pyCompileStopService(teamName, teamSeq);
        if (stopped.get("result").equals(SUCCESS)) {
            System.out.println("삭제 성공");
        }
        Optional<TeamEntity> teamEntity = teamRepository.findTeamPortByTeamSeq(Long.valueOf(teamSeq));
        if (!teamEntity.isPresent()) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }
        String port = teamEntity.get().getTeamPort();
        // 절대경로 생성
//        String absolutePath;
        // pure Python일 경우 파일명까지, 프로젝트일 경우 프로젝트명까지 절대경로로 선언
//        if (type == 1) {
//            absolutePath = filePath;
//        } else {
//            absolutePath = BASE_URL + teamSeq;
//        }

        String projectPath = BASE_URL + teamSeq;

        // 도커 파일 생성
        String dockerfile = createDockerfile(filePath, Long.valueOf(teamSeq), type, input);
        if (!Objects.equals(dockerfile, "SUCCESS")) {
            serviceRes.put("result", dockerfile);
            return serviceRes;
        }

        // 도커 이미지 빌드
        String[] image = {"docker", "build", "-t", conAndImgName, projectPath + "/"};
        String imageBuild = resultStringService(image);
        if (imageBuild.isEmpty()) {
            serviceRes.put("result", UNKNOWN);
            return serviceRes;
        }
        String insidePort;
        switch (type) {
            case 3:
                insidePort = ":5000";
                break;
            case 4:
                insidePort = ":8000";
                break;
            default:
                insidePort = ":3000";
                break;
        }

        // 도커 컨테이너 런
        String[] command = new String[]{"docker", "run", "-d", "--name", conAndImgName, "-v",
                    projectPath + ":" + projectPath, "-p", port + insidePort, conAndImgName};
//        if (type == 1) {
//            command = new String[]{"docker", "run", "-d", "--name", conAndImgName, "-v",
//                    projectPath + ":" + projectPath, "-p", port + insidePort, conAndImgName};
//        } else {
//            command = new String[]{"docker", "run", "-d", "--name", conAndImgName, "-p", port + insidePort, conAndImgName};
//        }

        // 결과 문자열
        String response = resultStringService(command);

        if (type == 1) {
            String[] pythonCmd = {"docker", "logs", "-f", conAndImgName};
            String pythonResponse = resultStringService(pythonCmd);
            String pathUpdateRes = pythonResponse.replace(BASE_URL, "");
            System.out.println(pythonResponse);
            serviceRes.put("result", SUCCESS);
            serviceRes.put("response", pathUpdateRes);
            return serviceRes;
        } else if (portNumService(conAndImgName).equals(port)) {
            serviceRes.put("result", SUCCESS);
            serviceRes.put("response", "k7d207.p.ssafy.io:" + port);
            return serviceRes;
        } else {
            serviceRes.put("result", SUCCESS);
            serviceRes.put("response", response);
            return serviceRes;
        }

    }

    /**
     * 컴파일 중단을 처리하는 내부로직
     *
     * @param teamName 컴파일 중단할 프로젝트의 팀 이름
     * @param teamSeq  컴파일 중단할 프로젝트의 팀 sequence
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> pyCompileStopService(String teamName, String teamSeq) {
        String conAndImgName = "crowstudio_" + teamName.toLowerCase().replaceAll(" ", "") + "_" + teamSeq;

        // 도커 컨테이너 멈추기
        String[] containerStop = {"docker", "stop", conAndImgName};
        Map<String, String> serviceRes = new HashMap<>();
        String stopedCon = resultStringService(containerStop);
        System.out.println("컨테이너 중지" + stopedCon);
        // 컨테이너가 없는 경우
//        if (stopedCon.equals("No such container")) {
//            serviceRes.put("result", NO_SUCH);
//            return serviceRes;
//        } else if (!stopedCon.equals(conAndImgName)) {
//            serviceRes.put("result", UNKNOWN);
//            return serviceRes;
//        }

        // 컨테이너 삭제
        String[] containerRm = {"docker", "rm", conAndImgName};
        String removedCon = resultStringService(containerRm);
        System.out.println("컨테이너 삭제" + removedCon);
        // 컨테이너가 없는 경우
//        if (removedCon.equals("No such container")) {
//            serviceRes.put("result", NO_SUCH);
//            return serviceRes;
//        } else if (!removedCon.equals(conAndImgName)) {
//            serviceRes.put("result", UNKNOWN);
//            return serviceRes;
//        }

        // 도커 이미지 삭제
        String[] imageRm = {"docker", "rmi", conAndImgName};
        String rmImg = resultStringService(imageRm);
        System.out.println("이미지 삭제" + rmImg);
        // 이미지가 없는 경우
//        if (rmImg.contains("No such image")) {
//            serviceRes.put("result", NO_SUCH);
//            return serviceRes;
//        } else if (rmImg.startsWith("Error")) {
//            serviceRes.put("result", UNKNOWN);
//            return serviceRes;
//        }

        // 도커파일 삭제
        Map<String, String> deletedFile = fileService.deleteFileService(
                BASE_URL + teamSeq + "/Dockerfile", 2, Long.parseLong(teamSeq));
        if (!deletedFile.get("result").equals(SUCCESS)) {
            serviceRes.put("result", deletedFile.get("result"));
            return serviceRes;
        }
        serviceRes.put("result", SUCCESS);
        return serviceRes;
    }

    /**
     * 팀 생성 시 자동으로 최초 컨테이너를 생성시켜 포트를 할당하는 내부 로직
     *
     * @param teamName 생성된 팀의 이름
     * @param teamSeq  생성된 팀의 Sequence
     * @return 생성된 컨테이너의 포트 번호, 성패에 따른 result 반환
     */
    public Map<String, String> containerCreateService(String teamName, Long teamSeq) {
        Map<String, String> serviceRes = new HashMap<>();
        String conAndImgName = "crowstudio_" + teamName.toLowerCase().replaceAll(" ", "") + "_" + teamSeq;
        // python docker image로 초기 컨테이너 생성 및 포트 할당
        String[] cmd = {"docker", "run", "-d", "--name", conAndImgName, "-p", "3000", "initialpython"};
        String result = resultStringService(cmd);
        System.out.println(result);
        // 포트번호 가져오기
        String portString = portNumService(conAndImgName);
        if (portString.equals(NO_SUCH)) {
            serviceRes.put("result", WRONG);
            return serviceRes;
        }
        if (result.startsWith("Error: No such container")) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }
        serviceRes.put("port", portString);
        serviceRes.put("result", SUCCESS);
        return serviceRes;
    }

    /**
     * 프로젝트 타입 구분하는 내부 로직
     *
     * @param filePath 구분할 프로젝트 경로 (teamSeq/teamName/...)
     * @return 프로젝트 타입 반환
     */
    public Map<String, String> findProjectTypeService(String filePath) {
        Map<String, String> serviceRes = new HashMap<>();
        String[] pathList = filePath.split("/");
        String projectPath = BASE_URL + pathList[0] + "/" + pathList[1] + "/";
        List<String> initialList = new ArrayList<>();
        List<String> fileList = showFilesInDIr(projectPath, initialList);
        if (fileList == null) {
            serviceRes.put("type", "0");
            return serviceRes;
        }
        // 통상적인 Django에 있는 manage.py가 있고 그 안에 특정 코드가 있으면 Django 프로젝트
        if (fileList.contains(BASE_URL + projectPath + "manage.py")) {
            Map<String, String> settingFile = fileService.readFileService(BASE_URL + projectPath + "manage.py");
            String settingContent = settingFile.get("fileContent");
            if (settingContent.contains(DJANGO)) {
                serviceRes.put("type", "2");
                serviceRes.put("path", BASE_URL + projectPath + "manage.py");
                return serviceRes;
            }
        }
        for (String file : fileList) {
            Map<String, String> fileContentRes = fileService.readFileService(file);
            String fileContent = fileContentRes.get("fileContent");
            if (fileContent.contains(FASTAPI)) {
                serviceRes.put("type", "4");
                serviceRes.put("path", file);
                return serviceRes;
            } else if (fileContent.contains(FLASK)) {
                serviceRes.put("type", "3");
                serviceRes.put("path", file);
                return serviceRes;
            } else if (fileContent.contains(DJANGO)) {
                serviceRes.put("type", "2");
                serviceRes.put("path", file);
                return serviceRes;
            }
        }
        serviceRes.put("type", "1");
        serviceRes.put("path", BASE_URL + filePath);
        return serviceRes;
    }

    /**
     * 프로젝트에 있는 모든 파일을 리스트로 반환하는 내부 로직
     *
     * @param filePath 파일 조회할 프로젝트 경로
     * @param fileList 현재 파일 리스트
     * @return 파일 리스트 반환
     */
    public List<String> showFilesInDIr(String filePath, List<String> fileList) {
        System.out.println(filePath);
        File dir = new File(filePath);
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                showFilesInDIr(file.getPath(), fileList);
            } else {
                fileList.add(file.getPath());
            }
        }
        return fileList;
    }
}
