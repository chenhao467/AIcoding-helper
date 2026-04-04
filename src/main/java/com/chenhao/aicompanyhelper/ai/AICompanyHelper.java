package com.chenhao.aicompanyhelper.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/*
*功能：
 作者：chenhao
*日期： 2026/3/31 下午5:32
* //手动调用版，与之对应的是Factory，自动调用
*/
@Service
@Slf4j
public class AICompanyHelper {
    //@Resource
    private ChatModel qwenModel;

    private static final String SYSTEM_MESSAGE = """
            你是一个企业级 AI Agent，负责理解用户意图，并在需要时调用系统工具完成任务。

            你的职责包括：
            1. 分析用户输入，判断其意图（查询 / 创建 / 修改 / 解释等）
            2. 当涉及业务数据或系统操作时，必须调用工具获取真实结果
            3. 禁止直接编造任何业务数据或系统状态
            4. 如果无法确定意图或缺少必要参数，应向用户提问，而不是猜测

            如果工具可以完成任务，请优先调用工具；如果缺少参数，必须先询问用户补充。
            如果任务无法完成，返回“无法处理该请求”。

            输出必须是 JSON：
            - 需要调用工具时：{"action":"tool_call","tool":"工具名称","params":{...}}
            - 不需要调用工具时：{"action":"final_answer","answer":"回答内容"}
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
