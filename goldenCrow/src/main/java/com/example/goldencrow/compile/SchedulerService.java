package com.example.goldencrow.compile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 자정에 사용자의 Docker Container와 images를 모두 멈추고 삭제하는 Service
 */
@Service
public class SchedulerService {

    @Autowired
    private CompileService compileService;

    /**
     * 매일 0시 0분 0초에 사용자의 Docker container와 image를 모두 삭제하는 내부 로직
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        // 모든 컨테이너 닫기
        /* 특정 이름을 가진 것만 필터링해서 조회 (crowstudio_)
         * docker stop $(docker container ls --filter=name=crowstudio_ -q)
         * 사용하지 않는 이미지 삭제(강경)
         * docker image prune -a -f
         * */
        String filteringName = "crowstudio_";
        String[] containerCmd = {"docker", "container", "ls", "--filter=name=" + filteringName, "-q"};
        String[] containerList = compileService.resultStringService(containerCmd).split("(\r\n|\r|\n|\n\r)");
        // 삭제할 container가 있으면 삭제
        if (containerList.length != 0) {
            for (String container :
                    containerList) {
                String[] stopCmd = {"docker", "stop", container};
                compileService.resultStringService(stopCmd);
            }
        }
        // 사용하지 않는 도커 이미지 모두 삭제
        String[] rmImgCmd = {"docker", "image", "prune", "-a", "-f"};
        compileService.resultStringService(rmImgCmd);
    }
}
