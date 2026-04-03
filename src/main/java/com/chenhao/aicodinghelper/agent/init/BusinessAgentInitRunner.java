package com.chenhao.aicodinghelper.agent.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BusinessAgentInitRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        // Flyway 在此之前已完成建表，这里仅做启动确认日志。
        log.info("Business Agent modules initialized: intent/tool/rag/ticket-service");
    }
}
