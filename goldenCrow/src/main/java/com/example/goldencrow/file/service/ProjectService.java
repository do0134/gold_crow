package com.example.goldencrow.file.service;

import com.example.goldencrow.file.FileEntity;
import com.example.goldencrow.file.FileRepository;

import com.example.goldencrow.file.dto.FileCreateDto;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

import static com.example.goldencrow.common.Constants.*;

/**
 * project 관련 로직을 처리하는 Service
 */
@Service
public class ProjectService {

    private final FileService fileService;
    @Autowired
    private final FileRepository fileRepository;

    /**
     * ProjectService 생성자
     *
     * @param fileService    file 관련 로직을 처리하는 Service
     * @param fileRepository file 관련 Repository
     */
    public ProjectService(FileService fileService, FileRepository fileRepository) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
    }

    /**
     * 폴더를 생성하는 내부 로직
     *
     * @param path 폴더를 생성할 경로
     * @param name 폴더의 이름
     * @return 생성한 폴더의 경로 혹은 DUPLICATE(이미 존재할 경우)
     */
    public String createDirService(String path, String name) {
        String pjt = path + name;
        File pjtDir = new File(pjt);
        if (pjtDir.mkdir()) {

            return pjt;
        }
        return DUPLICATE;
    }


    /**
     * 파일 경로 조회 내부 로직
     *
     * @param rootPath 조회하려는 경로
     * @param rootName 조회하려는 파일(폴더)명
     * @param visit    현재까지의 파일 경로들이 저장된 Map
     * @return 파일경로를 조회해 반환
     */
    public Map<Object, Object> readDirectoryService(String rootPath, String rootName, Map<Object, Object> visit) {
        String path = BASE_URL + rootPath;
        File file = new File(path);
        visit.put("id", rootPath);
        visit.put("name", rootName);
        // 폴더일 경우
        if (file.isDirectory()) {
            List<Object> child = new ArrayList<>();
            File[] files = file.listFiles();
            String[] names = file.list();
            if (files == null) {
                Map<Object, Object> errorValue = new HashMap<>();
                errorValue.put("error", NO_SUCH);
                return errorValue;
            }
            if (names == null) {
                Map<Object, Object> errorValue = new HashMap<>();
                errorValue.put("error", NO_SUCH);
                return errorValue;
            }
            for (int i = 0; i < files.length; i++) {
                File dir = files[i];
                String name = names[i];
                if (name.equals("Dockerfile")) {
                    continue;
                }
                if (name.equals(".git")) {
                    continue;
                }
                String thisPath = dir.getPath();
                thisPath = thisPath.replace(BASE_URL, "");
                Map<Object, Object> children = new HashMap<>();
                child.add(readDirectoryService(thisPath, name, children));
            }
            visit.put("children", child);
            visit.put("type", "folder");
        } else {
            String fileType = checkNameService(file.getName());
            visit.put("type", fileType);
        }
        return visit;
    }

    /**
     * 파일 이름에 무엇이 포함되냐에 따라 파일 종류 나누는 내부 로직
     *
     * @param fileName 판별할 파일 이름
     * @return python, html, css, js, text
     */
    public String checkNameService(String fileName) {
        if (fileName.contains(".py")) {
            return "python";
        } else if (fileName.contains(".html")) {
            return "html";
        } else if (fileName.contains(".js")) {
            return "js";
        } else if (fileName.contains(".css")) {
            return "css";
        }
        return "text";
    }

    /**
     * 모든 경로를 재귀적으로 찾고, db에 저장하는 내부 로직
     *
     * @param path    조회하는 경로
     * @param teamSeq 해당 프로젝트의 팀 Sequence
     */
    public void saveFilesInDIrService(String path, Long teamSeq) {
        File file = new File(path);

        File[] files = file.listFiles();
        if (files != null) {
            for (File dir : files) {

                FileCreateDto newFileCreateDto = new FileCreateDto(dir.getName(), dir.getPath(), teamSeq);
                // file 저장
                fileService.insertFileService(newFileCreateDto);
                // Directory일 경우
                if (dir.isDirectory()) {
                    String thisPath = dir.getPath();
                    if (dir.getName().equals(".git")) {
                        continue;
                    }
                    saveFilesInDIrService(thisPath, teamSeq);
                }
            }
        }
    }

    /**
     * 프로젝트 생성 내부 로직
     *
     * @param type        생성할 프로젝트의 종류 (1: pure Python, 2: Django, 3: Flask, 4: FastAPI)
     * @param projectName 생성할 프로젝트의 이름
     * @param teamSeq     생성할 프로젝트의 팀 Sequence
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> createProjectService(int type, String projectName, Long teamSeq) {
        Map<String, String> serviceRes = new HashMap<>();
        String teamFile = createDirService(BASE_URL,String.valueOf(teamSeq));

        if (teamFile.equals(DUPLICATE)) {
            serviceRes.put("result", DUPLICATE);
            return serviceRes;
        }

        // 기본 프로젝트 구성, 기본 파일 생성
        if (type == 2) {
            ProcessBuilder djangoStarter = new ProcessBuilder();
            djangoStarter.command("django-admin", "startproject", projectName);
            djangoStarter.directory(new File(teamFile));
            StringBuilder sb = new StringBuilder();
            try {
                String read;
                Process p = djangoStarter.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((read = br.readLine()) != null) {
                    sb.append(read);
                }
                p.waitFor();
            } catch (IOException e) {
                serviceRes.put("result", UNKNOWN);
                return serviceRes;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                serviceRes.put("result", UNKNOWN);
                return serviceRes;
            }

            String message = sb.toString();

            if (message.contains("CommandError")) {
                serviceRes.put("result",WRONG);
                return serviceRes;
            }

            String newPath = teamFile + "/" + projectName + "/" + projectName + "/" + "settings.py";
            String changeSetting = changeSettingService(newPath);

            if (!changeSetting.equals(SUCCESS)) {
                serviceRes.put("result", UNKNOWN);
                return serviceRes;
            }

            // file 저장
            String pjtPath = teamFile + "/" + projectName;
            saveFilesInDIrService(pjtPath, teamSeq);
            serviceRes.put("result", SUCCESS);
            return serviceRes;
        } else if (type == 1) {
            String pjt = createDirService(teamFile+"/", projectName);
            if (pjt.equals(DUPLICATE)) {
                serviceRes.put("result", DUPLICATE);
                return serviceRes;
            }

            File file = new File(pjt + "/" + projectName + ".py");
            try {
                if (file.createNewFile()) {
                    saveFilesInDIrService(pjt, teamSeq);
                    serviceRes.put("result", SUCCESS);
                } else {
                    serviceRes.put("result", DUPLICATE);
                }
                return serviceRes;
            } catch (IOException e) {
                serviceRes.put("result", UNKNOWN);
                return serviceRes;
            }
        } else if (type == 3) {
            String pjt = createDirService(teamFile+"/", projectName);
            if (pjt.equals(DUPLICATE)) {
                serviceRes.put("result", DUPLICATE);
                return serviceRes;
            }
            File file = new File(pjt + "/main.py");

            String content = "from flask import Flask\n" +
                    "import sys\n" +
                    "sys.path.append('/prod/app')\n\n" +
                    "app=Flask(__name__)\n\n" +
                    "@app.route(\"/\")\n" +
                    "def hello_world():\n" +
                    "\treturn \"<p>Hello, World</p>\" \n\n" +
                    "if __name__ == \"__main__\" :\n" +
                    "\tapp.run(\"0.0.0.0\")";

            // 파일에 내용 저장
            try (FileWriter overWriteFile = new FileWriter(file, false)) {
                overWriteFile.write(content);
            } catch (IOException e) {
                serviceRes.put("result", UNKNOWN);
                return serviceRes;
            }
            saveFilesInDIrService(pjt, teamSeq);
            serviceRes.put("result", SUCCESS);
            return serviceRes;
        } else if (type == 4) {
            String pjt = createDirService(teamFile + "/", projectName);
            if (pjt.equals(DUPLICATE)) {
                serviceRes.put("result", DUPLICATE);
                return serviceRes;
            }

            File file = new File(pjt + "/main.py");

            // main.py에 저장할 내용
            String content = "from fastapi import FastAPI\n" +
                    "import sys\nsys.path.append('/prod/app')\n\n" +
                    "app=FastAPI()\n\n" +
                    "@app.get(\"/\")\n" +
                    "async def root():\n\t" +
                    "return {\"message\" : \"Hello, World\"}";

            // 파일에 내용 저장
            try (FileWriter overWriteFile = new FileWriter(file, false)) {
                overWriteFile.write(content);
            } catch (IOException e) {
                serviceRes.put("result", UNKNOWN);
                return serviceRes;
            }

            saveFilesInDIrService(pjt, teamSeq);
            serviceRes.put("result", SUCCESS);
            return serviceRes;
        }
        serviceRes.put("result", UNKNOWN);
        return serviceRes;
    }

    /**
     * 팀 리스트에 속한 프로젝트를 모두 삭제하는 내부 로직
     *
     * @param teamSeqList 삭제하고자 하는 팀의 시퀀스로 이루어진 리스트
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> deleteProjectService(List<Long> teamSeqList) {
        Map<String, String> serviceRes = new HashMap<>();
        // 삭제 로직 수행
        try {
            ProcessBuilder deleter = new ProcessBuilder();
            for (Long seq : teamSeqList) {
                // 서버에서 프로젝트 삭제
                deleter.command("rm", "-r", String.valueOf(seq));
                deleter.directory(new File(BASE_URL));
                try {
                    deleter.start();
                } catch (IOException e) {
                    serviceRes.put("result", UNKNOWN);
                    return serviceRes;
                }
                // DB에서 프로젝트 삭제
                String pjtDeleted = pjtFileDeleteService(seq);
                if (!pjtDeleted.equals(SUCCESS)) {
                    serviceRes.put("result", pjtDeleted);
                    return serviceRes;
                }
            }
            serviceRes.put("result", SUCCESS);
        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);
        }
        return serviceRes;
    }

    /**
     * 해당 시퀀스에 해당하는 DB의 데이터를 모두 삭제하는 내부 로직
     *
     * @param teamSeq 삭제하려는 팀의 Sequence
     */
    public String pjtFileDeleteService(Long teamSeq) {
        Optional<List<FileEntity>> files = fileRepository.findAllByTeamSeq(teamSeq);
        if (!files.isPresent()) {
            return NO_SUCH;
        }
        List<FileEntity> deleteFile = files.get();
        fileRepository.deleteAll(deleteFile);
        return SUCCESS;
    }


    /**
     * Django project의 settings.py에서
     * ALLOWED_HOSTS에 서버 주소를 넣어 배포가 가능하게 하는 내부 로직
     *
     * @param filePath 파일의 경로 (파일 이름까지 포함)
     * @return 성패에 따른 result string 반환
     */
    public String changeSettingService(String filePath) {
        String oldFileName = "settings.py";
        String tmpFileName = "tmp_settings.py";
        String newFilePath = filePath.replace(oldFileName, tmpFileName);

        // 기존 settings.py의 내용을 불러오되 ALLOWED_HOST 부분 수정하여 저장
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(newFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("ALLOWED_HOSTS = []")) {
                    line = line.replace("ALLOWED_HOSTS = []",
                            "ALLOWED_HOSTS = [\"k7d207.p.ssafy.io\"]");
                }
                bw.write(line + "\n");
            }
        } catch (Exception e) {
            return UNKNOWN;
        }

        // 변경된 settings.py를 저장할 경로
        String newPath = filePath.replace(oldFileName, "");

        // tmpFileName을 'settings.py'로 변경하는 명령어
        ProcessBuilder pro = new ProcessBuilder("mv", tmpFileName, oldFileName);
        pro.directory(new File(newPath));
        try {
            pro.start();
        } catch (IOException e) {
            return UNKNOWN;
        }
        return SUCCESS;
    }
}
