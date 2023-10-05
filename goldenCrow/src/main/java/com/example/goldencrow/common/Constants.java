package com.example.goldencrow.common;

/**
 * 프로젝트 내 상수를 관리하는 클래스
 */
public final class Constants {

    /**
     * 로직을 성공적으로 수행한 결과
     */
    public static final String SUCCESS = "SUCCESS";

    /**
     * 입력이 들어오지 않아 로직을 수행하지 못한 결과
     */
    public static final String BAD_REQ = "BAD REQUEST...";
    /**
     * 알 수 없는 이유로 로직을 수행하지 못한 결과
     */
    public static final String UNKNOWN = "UNKNOWN ERROR...";

    /**
     * 권한 불충분으로 로직을 수행하지 못한 결과
     */
    public static final String NO_PER = "NO PERMISSION...";

    /**
     * 특정 사용자, 파일, 폴더 등을 찾지 못하여 로직을 수행하지 못한 결과
     */
    public static final String NO_SUCH = "NO SUCH THING...";

    /**
     * 데이터 중복으로 로직을 수행하지 못한 결과
     */
    public static final String DUPLICATE = "DUPLICATE...";

    /**
     * 잘못된 데이터 입력으로 로직을 수행하지 못한 결과
     */
    public static final String WRONG = "WRONG DATA...";

    /**
    * 프로젝트의 데이터를 저장할 기본 URL
    */
    public static final String BASE_URL = "/home/ubuntu/crow_data/";

}
