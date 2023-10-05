package com.example.goldencrow.variable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.goldencrow.common.Constants.*;

/**
 * 변수명 추천과 관련된 로직을 처리하는 Service
 */
@Service
public class VariableService {

    public RestTemplate restTemplate = new RestTemplate();
    /**
     * Header를 생성하기 위해 필요한 id와 pass
     */
    @Value("${secret.client.id}")
    private String CLIENT_ID;

    @Value("${secret.client.pass}")
    private String CLIENT_PASS;

    /**
     * 변수명 추천 내부 로직
     *
     * @param data  변수명을 추천받을 한글 단어
     * @return 추천받은 변수명 리스트 반환, 성패에 따른 result 반환
     */

    public Map<String, Object> variableRecommendService(String data) {
        Map<String, Object> serviceRes = new HashMap<>();
        /**
         * words : 다양한 api를 활용해 단어를 번역하여 담기위한 리스트
         * 현재는 papago api만을 활용하고 있다
         */
        ArrayList<String> words = new ArrayList<>();

        // papago api를 활용해 번역
        String papagoWord = papagoApiService(data);
        if (papagoWord.equals(NO_SUCH)) {
            serviceRes.put("result", NO_SUCH);
            return serviceRes;
        }
        words.add(papagoWord);

        // camel, pascal, snake 타입으로 변경된 단어들을 저장할 리스트
        ArrayList<String> converts = new ArrayList<>();

        for (String word : words) {
            String[] letters = word.split(" ");
            StringBuilder camel = new StringBuilder();
            StringBuilder pascal = new StringBuilder();
            StringBuilder snake = new StringBuilder();
            for (String letter : letters) {
                String clearLetter = letter.replaceAll("%20", "");
                // toLowerCase().equals => equalsIgnoreCase()로 메서드 사용 최소화
                if (!clearLetter.equalsIgnoreCase("a") && !clearLetter.equalsIgnoreCase("an")) {
                    /** camel 타입 변수 생성
                     * 첫번째 단어일 경우 첫번째 알파벳은 소문자, 나머지 알파벳은 소문자
                     * 첫번째 단어가 아닐 경우 첫번째 알파벳은 대문자, 나머지 알파벳은 소문자
                     */
                    if (camel.length() == 0) {
                        camel.append(letter.toLowerCase());
                    } else {
                        camel.append(letter.substring(0, 1).toUpperCase())
                                .append(letter.substring(1));
                    }

                    /** pascal 타입 변수 생성
                     * 첫번째 단어일 경우 첫번째 알파벳은 대문자, 나머지 알파벳은 소문자
                     * 첫번째 단어가 아닐 경우 첫번째 알파벳은 대문자, 나머지 알파벳은 소문자
                     */
                    if (pascal.length() == 0) {
                        pascal.append(letter.substring(0, 1).toUpperCase())
                                .append(letter.substring(1).toLowerCase());
                    } else {
                        pascal.append(letter.substring(0, 1).toUpperCase())
                                .append(letter.substring(1));
                    }
                    /** snake 타입 변수 생성
                     * 첫번째 단어일 경우 모든 알파벳은 소문자
                     * 첫번째 단어가 아닐 경우 앞에 "_" 넣고 모든 알파벳은 소문자
                     */
                    if (snake.length() == 0) {
                        snake.append(letter.toLowerCase());
                    } else {
                        snake.append("_").append(letter.toLowerCase());
                    }
                }
            }
            converts.add(camel.toString());
            converts.add(pascal.toString());
            converts.add(snake.toString());
        }
        serviceRes.put("result", SUCCESS);
        serviceRes.put("data", converts);
        return serviceRes;
    }

    /**
     * 파파고 API 사용하는 내부 로직
     *
     * @param word  번역에 사용할 단어(한글)
     * @return 성공 시 번역한 단어 반환, 실패 시 실패코드 반환
     */
    public String papagoApiService(String word) {
        String serviceRes = "";
        // papago api에 활용할 uri 생성
        URI uri = UriComponentsBuilder.fromUriString("https://openapi.naver.com")
                .path("/v1/papago/n2mt")
                .queryParam("source", "ko")
                .queryParam("target", "en")
                .queryParam("text", word.replaceAll(" ", "")).build().toUri();
        // naver header 생성
        RequestEntity<Void> requestEntity = naverHeaderService(uri);

        ResponseEntity<String> restTemplateResult
                = restTemplate.exchange(uri.toString(), HttpMethod.POST, requestEntity, String.class);

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(restTemplateResult.getBody());
            JSONObject message = (JSONObject) jsonObject.get("message");
            JSONObject result = (JSONObject) message.get("result");
            serviceRes = result.get("translatedText").toString();
        } catch (Exception e) {
            return UNKNOWN;
        }
        return serviceRes;
    }

    /**
     * naver Header를 생성하는 내부 로직
     *
     * @param uri   naver Header를 생성하기위해 필요한 URI
     * @return naver Header을 생성해 RequestEntity<Void>로 반환
     */
    private RequestEntity<Void> naverHeaderService(URI uri) {
        return RequestEntity.get(uri).header("X-Naver-Client-Id", "p3IEa7WNGODfNQkwb1z2")
                .header("X-Naver-Client-Secret", "MSAQnEbctM").build();
    }

//    public String googleApi(String word) {
//        String key = "AIzaSyAyIwR6h1kVLg7b-cKpeB5Xm6nUza4aj3M";
//        String targetLanguage = "en";
//        String projectId = "teamgoldencrow";
//
//        try (TranslationServiceClient client = TranslationServiceClient.create()) {
//            LocationName parent = LocationName.of(projectId, "global");
//
//            TranslateTextRequest request =
//                    TranslateTextRequest.newBuilder()
//                            .setParent(parent.toString())
//                            .setMimeType("text/plain")
//                            .setTargetLanguageCode(targetLanguage)
//                            .addContents(word)
//                            .build();
//
//            TranslateTextResponse response = client.translateText(request);
//
//            for (Translation translation : response.getTranslationsList()) {
//                System.out.println("Google " + translation.getTranslatedText());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return "1";
//
//    }
}
