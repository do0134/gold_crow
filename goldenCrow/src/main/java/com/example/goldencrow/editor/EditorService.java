package com.example.goldencrow.editor;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.example.goldencrow.common.Constants.*;

/**
 * 코드의 스타일(포맷팅), 문법검사(린트)를 처리하는 Service
 */
@Service
public class EditorService {
    public static final String PATH = BASE_URL + "temp/";

    /**
     * 포맷팅을 처리하는 내부 로직
     *
     * @param language 해당 파일의 언어 종류 ex. python, text
     * @param code     해당 파일의 내용
     * @return 포맷팅 처리를 한 temp 파일의 제목, 성패에 따른 result 반환
     */
    public Map<String, String> formatService(String language, String code) {
        long now = new Date().getTime();
        Map<String, String> serviceRes = new HashMap<>();
        String type;
        // 해당 파일의 언어 확인
        switch (language) {
            case "python":
                type = ".py";
                break;
            default:
                type = ".txt";
                break;
        }

        try {
            // 현재 시간을 활용해 temp 파일명
            String name = "format" + now + type;
            // temp 파일 생성
            File file = new File(PATH + name);
            FileOutputStream ffw = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(ffw);
            // temp 파일에 code를 입력
            writer.print(code);

            // FileWriter 닫기
            writer.flush();
            writer.close();

            // ubuntu에서 포맷팅을 위해 black을 실행시키는 명령어
            String command = "black " + PATH + name;

            // Black 작동
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            serviceRes.put("result", SUCCESS);
            serviceRes.put("data", now + "");
        } catch (Exception e) {
            serviceRes.put("result", UNKNOWN);
        }
        return serviceRes;
    }

    /**
     * 포맷팅 결과를 읽어오는 내부 로직
     *
     * @param language 해당 파일의 언어 종류 ex. python, text
     * @param fileName 포맷팅한 결과가 저장되어있는 파일의 이름
     * @return 포맷팅한 결과 코드를 반환, 성패에 따른 result 반환
     */
    public Map<String, String> formatReadService(String language, String fileName) {
        Map<String, String> serviceRes = new HashMap<>();
        String type;
        // 파일의 언어 종류
        if (language.equals("python")) {
            type = ".py";
        } else {
            type = ".txt";
        }
        // 파일의 절대 경로
        String absolutePath = PATH + "format" + fileName + type;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(absolutePath));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                String temp = str + "\n";
                sb.append(temp);
            }
            serviceRes.put("data", sb.toString());
            reader.close();
        } catch (Exception e) {
            // 파일의 경로가 틀렸거나 그 파일이 없는 경우
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }

        // 사용한 temp파일 삭제
        Path filePath = Paths.get(absolutePath);
        try {
            Files.deleteIfExists(filePath);
            // 파일 삭제까지 모두 수행한 경우
            serviceRes.put("result", SUCCESS);
        } catch (Exception e) {
            // 파일 삭제가 제대로 수행되지 않은 경우
            serviceRes.put("result", UNKNOWN);
        }
        return serviceRes;
    }

    /**
     * 린트를 처리하는 내부 로직
     *
     * @param language 해당 파일의 언어 종류 ex. python
     * @param code     해당 파일의 내용
     * @return
     */
    public Map<String, Object> lintService(String language, String code) {
        Map<String, Object> serviceRes = new HashMap<>();
        String filePath;
        if (language.equals("python")) {
            try {
                File file = new File(PATH + "lint.py");
                FileOutputStream lfw = new FileOutputStream(file);
                PrintWriter writer = new PrintWriter(lfw);
                // missing-module-docstring, missing-final-newline 오류는 잡지 않도록 조정
                writer.println("# pylint: disable=C0114, C0304");
                // temp.py에 code를 입력
                writer.print(code);

                // FileWriter 닫기
                writer.flush();
                writer.close();

                filePath = PATH + "lint.py";
                String command = "pylint " + filePath;
                // pylint를 활용해 lint 명령어 실행
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                // 결과에서 index값을 저장할 indexList, 결과값을 저장할 responseList 저장
                LinkedList<String> responseList = new LinkedList<>();
                ArrayList<Integer> indexList = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    if (line.contains("lint.py")) {
                        String[] letters = line.split(":");
                        int number = Integer.parseInt(letters[1]);
                        indexList.add(number);
                        responseList.add(letters[4].trim());
                    }
                }
                reader.close();
                serviceRes.put("data", responseList);
                serviceRes.put("index", indexList);
            } catch (Exception e) {
                serviceRes.put("result", NO_SUCH);
                return serviceRes;
            }
        } else {
            serviceRes.put("result", WRONG);
            return serviceRes;
        }

        Path path = Paths.get(filePath);
        try {
            Files.deleteIfExists(path);
            serviceRes.put("result", SUCCESS);
        } catch (IOException ioe) {
            serviceRes.put("result", UNKNOWN);
        }
        return serviceRes;
    }
}
