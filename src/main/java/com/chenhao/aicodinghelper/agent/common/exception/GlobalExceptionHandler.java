package com.chenhao.aicodinghelper.agent.common.exception;

import com.chenhao.aicodinghelper.agent.vo.AgentChatResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public AgentChatResponse handleBizException(BizException ex) {
        return AgentChatResponse.failed(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public AgentChatResponse handleUnknownException(Exception ex) {
        return AgentChatResponse.failed("系统异常: " + ex.getMessage());
    }
}
