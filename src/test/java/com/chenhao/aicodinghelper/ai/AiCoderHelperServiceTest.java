package com.chenhao.aicodinghelper.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiCoderHelperServiceTest {
    @Resource
    private AiCoderHelperService aiCoderHelperService;
    @Test
    void chatForReport() {
        String userMessage = "你好，我是陈昊，请帮我制定学习报告";
        AiCoderHelperService.Report report = aiCoderHelperService.chatForReport(userMessage);
        System.out.println(report);
    }
}