package com.chenhao.aicompanyhelper.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCoderHelperServiceTest {
    @Resource
    private AiCompanyHelperService aiCompanyHelperService;
    @Test
    void chatForReport() {
        String userMessage = "你好，我是陈昊，请帮我制定学习报告";
        AiCompanyHelperService.Report report = aiCompanyHelperService.chatForReport(userMessage);
        System.out.println(report);
    }
}