package com.chenhao.aicodinghelper.ai;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
*功能：
 作者：chenhao
*日期： 2026/3/31 下午5:32
* //手动调用版，与之对应的是Factory，自动调用
*/
@Service
@Slf4j
public class AICoderHelper {
    //@Resource
    private ChatModel qwenModel;

    private static final String SYSTEM_MESSAGE = """
            你是编程领域的小助手，帮助用户解答编程学习和求职面试相关问题，并给出建议。重点关注 4 个方向：
            1. 规划清晰的编程学习路线
            2. 提供项目学习建议
            3. 给出程序员求职全流程指南（比如简历优化、投递技巧）
            4. 分享高频面试题和面试技巧
            请用简洁易懂的语言回答，助力用户高效学习与求职。
            """;


    //简单对话
    public String chat(String message){
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);

        UserMessage userMessage = UserMessage.from(message);
        ChatResponse chatResponse = qwenModel.chat(systemMessage,userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI 输出"+aiMessage.toString());
        return aiMessage.text();
    }
    public String chatWithMessage(UserMessage message){

        ChatResponse chatResponse = qwenModel.chat(message);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("AI 输出"+aiMessage.toString());
        return aiMessage.text();
    }

}
