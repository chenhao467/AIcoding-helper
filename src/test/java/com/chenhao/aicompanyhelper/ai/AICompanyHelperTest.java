package com.chenhao.aicompanyhelper.ai;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AICompanyHelperTest {
    @Autowired
    private AICompanyHelper aiCompanyHelper;
    @Resource
    private AiCompanyHelperService aiCompanyHelperService;
    @Test
    void chat(){
        aiCompanyHelper.chat("你好，我是陈昊");
    }
    @Test
    void chatWithService(){
        String result = aiCompanyHelperService.chat("你好，我是陈昊");
        System.out.println(result);
    }
    @Test
    void chatWithMessage() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("描述图片"),
                ImageContent.from("https://zh.freepik.com/%E5%85%8D%E8%B2%BB%E5%9C%96%E7%89%87/professional-photographer-takes-photos-with-camera-tripod-rocky-peak-sunset_11599636.htm#fromView=keyword&page=1&position=0&uuid=bde42202-b600-49b2-b178-db1e94690dbe&query=%E5%9B%BE%E7%89%87")

        );
        aiCompanyHelper.chatWithMessage(userMessage);
    }
    @Test
    void chatWithMemory(){
        String result = aiCompanyHelperService.chat("你好我是陈昊");
        System.out.println(result);
        result = aiCompanyHelperService.chat("还记得我是谁吗");
        System.out.println(result);
    }

    @Test
    void chatWithRag(){
        String msg = aiCompanyHelperService.chat("系统里司机和企业的关系如何");
        System.out.println(msg);
    }

    @Test
    void chatWithResultRag(){
        Result<String> result = aiCompanyHelperService.chatWithRag("系统里司机和企业的关系如何");
        System.out.println(result.sources());
        System.out.println(result.content());

    }

    @Test
    void chatWithTools(){
        String result = aiCompanyHelperService.chat("有哪些常见的计算机网络面试题？");
        System.out.println(result);
    }
    @Test
    void chatWithMCP(){
        String result = aiCompanyHelperService.chat("什么是洛克王国，官方网址是什么？");
        System.out.println(result);
    }
}