package com.example.goldencrow.file.service;

import com.example.goldencrow.file.FileEntity;
import com.example.goldencrow.file.FileRepository;
import com.example.goldencrow.file.dto.FileCreateDto;
import com.example.goldencrow.file.dto.FileCreateRequestDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.Optional;

import static com.example.goldencrow.common.Constants.*;

/**
 * file 관련 로직을 처리하는 Service
 */
@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    /**
     * 파일(폴더) 생성 내부 로직
     *
     * @param teamSeq              파일(폴더)를 생성할 팀의 sequence
     * @param type                 생성할 문서의 종류 (1 : 폴더, 2 : 파일)
     * @param fileCreateRequestDto "fileTitle", "filePath"를 key로 가지는 Dto
     * @return 파일(폴더) 생성 성공 시 파일 경로 반환, 성패에 대한 result 반환
     */
    public Map<String, String> createFileService(Long teamSeq, int type, FileCreateRequestDto fileCreateRequestDto) {
        Map<String, String> serviceRes = new HashMap<>();
        String filePath = BASE_URL+ fileCreateRequestDto.getFilePath();
        File checkFile = new File(filePath);

        if (!checkFile.isDirectory()) {
            Optional<FileEntity> baseFile = fileRepository.findFileEntityByTeamSeqAndFilePath(teamSeq,filePath);
            if (!baseFile.isPresent()) {
                System.out.println("여긴가?");
                System.out.println(filePath);
                serviceRes.put("result",NO_SUCH);
                return serviceRes;
            }
            String baseFileName = baseFile.get().getFileTitle();

            filePath = filePath.replace("/"+baseFileName,"");
        }
        // 생성할 파일 혹은 폴더의 경로
        String newFilePath = filePath + "/" + fileCreateRequestDto.getFileTitle();

        // 경로와 타입으로 file 생성 로직 수행
        String makeNewFileRes = makeNewFileService(newFilePath, type);
        if (makeNewFileRes.equals(SUCCESS)) {
            FileCreateDto newFileCreateDto = new FileCreateDto(fileCreateRequestDto.getFileTitle(), newFilePath, teamSeq);
            insertFileService(newFileCreateDto);
            serviceRes.put("result", SUCCESS);
            serviceRes.put("filePath", newFilePath);
            return serviceRes;
        }
        serviceRes.put("result", UNKNOWN);
        return serviceRes;
    }

    /**
     * 서버에 파일을 생성하는 내부 로직
     *
     * @param filePath 파일을 생성할 경로
     * @param type     생성할 문서의 종류 (1 : 폴더, 2 : 파일)
     * @return 성패에 따른 result string 반환
     */
    public String makeNewFileService(String filePath, int type) {
        File newFile = new File(filePath);
        try {
            if (type == 1) {
                if (newFile.mkdir()) {
                    return SUCCESS;
                } else {
                    return DUPLICATE;
                }
            } else {
                if (newFile.createNewFile()) {
                    return SUCCESS;
                } else {
                    return DUPLICATE;
                }
            }
        } catch (IOException e) {
            return UNKNOWN;
        }
    }

    /**
     * MongoDB에 파일을 insert하는 내부 로직
     *
     * @param fileCreateDto insert할 파일 정보
     */
    public void insertFileService(FileCreateDto fileCreateDto) {
        FileEntity fileEntity = new FileEntity(fileCreateDto);
        fileRepository.insert(fileEntity);
    }

    /**
     * 파일(폴더) 삭제 관련 내부 로직
     *
     * @param filePath 삭제할 파일(폴더)의 경로
     * @param type     삭제할 문서의 종류 (1 : 폴더, 2 : 파일)
     * @param teamSeq  삭제할 문서의 프로젝트(팀) Sequence
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> deleteFileService(String filePath, int type, Long teamSeq) {
        Optional<FileEntity> file = fileRepository.findFileEntityByTeamSeqAndFilePath(teamSeq, filePath);
        Map<String, String> serviceRes = new HashMap<>();
        System.out.println(filePath);

        // DB에 없는 파일 경로인 경우
        if (!file.isPresent()) {
            System.out.println("DB에서 터짐");
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }
        // 서버에서 파일 삭제 로직 수행
        String fileDelete = serverFileDeleteService(type, filePath);
        if (!fileDelete.equals(SUCCESS)) {
            serviceRes.put("result", fileDelete);
            return serviceRes;
        }
        // DB에서 파일 삭제 로직 수행
        fileRepository.delete(file.get());
        serviceRes.put("result", SUCCESS);
        return serviceRes;
    }

    /**
     * 서버에서 파일(폴더) 삭제 내부 로직
     *
     * @param type     삭제할 문서의 종류 (1 : 폴더, 2 : 파일)
     * @param filePath 삭제할 파일(폴더)의 경로
     * @return 성패에 따른 result 반환
     */
    public String serverFileDeleteService(int type, String filePath) {
        Path path = Paths.get(filePath);
        // 디렉토리인 경우
        if (type == 1) {
            ProcessBuilder pb = new ProcessBuilder();
            // 디렉토리 삭제 명령어
            pb.command("rm", "-r", filePath);
            // 명령어 수행 로직
            try {
                pb.start();
            } catch (IOException e) {
                return e.getMessage();
            }
            // 파일인 경우
        } else {
            try {
                Files.delete(path);
            } catch (NoSuchFileException e) {
                return NO_SUCH;
            } catch (IOException ioe) {
                return UNKNOWN;
            }
        }
        return SUCCESS;
    }

    /**
     * 파일 내용 저장 내부 로직
     * 파일 저장 기존 파일을 삭제하고 새로운 파일을 덮어씌우는 형태
     *
     * @param filePath 저장할 파일의 경로
     * @param content  저장할 내용
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> saveFileService(String filePath, String content) {
        Map<String, String> serviceRes = new HashMap<>();

        // 원래 있던 파일 조회
        File oldFile;
        try {
            oldFile = new File(filePath);
        } catch (NullPointerException e) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        // 파일 삭제
        if (!oldFile.delete()) {
            serviceRes.put("result", UNKNOWN);
            return serviceRes;
        }

        // 새로운 파일 생성 & 내용 저장
        File newFile = new File(filePath);
        try (FileWriter overWriteFile = new FileWriter(newFile, false)) {
            overWriteFile.write(content);
            serviceRes.put("result", SUCCESS);
        } catch (IOException e) {
            serviceRes.put("result", UNKNOWN);
        }
        return serviceRes;
    }

    /**
     * 파일 이름 변경 내부 로직
     *
     * @param filePath    이름을 변경할 파일 경로
     * @param newFileName 변경할 파일 이름
     * @param oldFileName 현재 파일 이름
     * @param teamSeq     변경할 파일의 프로젝트(팀)의 Sequence
     * @return 성패에 따른 result 반환
     */
    public Map<String, String> updateFileNameService(String filePath, String newFileName, String oldFileName, Long teamSeq) {
        Map<String, String> serviceRes = new HashMap<>();

        // 파일명을 변경한 파일 경로
        String renameFilePath = filePath.replace(oldFileName, newFileName);
        File targetFile = new File(filePath);
        File reNameFile = new File(renameFilePath);

        Optional<FileEntity> file = fileRepository.findFileEntityByTeamSeqAndFilePath(teamSeq, filePath);
        if (!file.isPresent()) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        // DB에 변경된 경로와 파일명 저장
        FileEntity nameFile = file.get();
        nameFile.setFilePath(renameFilePath);
        nameFile.setFileTitle(newFileName);
        fileRepository.save(nameFile);

        // 파일명 변경
        if (targetFile.renameTo(reNameFile)) {
            serviceRes.put("result", SUCCESS);
        } else {
            serviceRes.put("result", UNKNOWN);
        }
        return serviceRes;
    }

    /**
     * 파일의 내용을 조회하는 내부 로직
     *
     * @param filePath 내용을 조회할 파일의 경로
     * @return 파일의 내용을 반환, 성패에 따른 result 반환
     */
    public Map<String, String> readFileService(String filePath) {
        Map<String, String> serviceRes = new HashMap<>();

        // 내용 조회 로직
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            serviceRes.put("result", NO_SUCH);
            serviceRes.put("fileContent", e.getMessage());
            return serviceRes;
        }
        serviceRes.put("result", SUCCESS);
        serviceRes.put("fileContent", content.toString());
        return serviceRes;
    }
}
