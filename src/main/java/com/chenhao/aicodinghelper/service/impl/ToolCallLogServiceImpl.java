package com.chenhao.aicodinghelper.service.impl;

import com.chenhao.aicodinghelper.common.exception.BizException;
import com.chenhao.aicodinghelper.entity.ToolCallLogEntity;
import com.chenhao.aicodinghelper.mapper.ToolCallLogMapper;
import com.chenhao.aicodinghelper.service.ToolCallLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolCallLogServiceImpl implements ToolCallLogService {

    private final ToolCallLogMapper toolCallLogMapper;

    @Override
    public void saveSuccess(String traceId, String toolName, String requestJson, String responseJson, Long ticketId, Long tenantId) {
        ToolCallLogEntity toolCallLog = new ToolCallLogEntity();
        toolCallLog.setTraceId(traceId);
        toolCallLog.setToolName(toolName);
        toolCallLog.setRequestJson(requestJson);
        toolCallLog.setResponseJson(responseJson);
        toolCallLog.setSuccess(1);
        toolCallLog.setTicketId(ticketId);
        toolCallLog.setTenantId(tenantId);
        try {
            toolCallLogMapper.insert(toolCallLog);
        }catch (Exception e){
            log.info("工具调用记录服务出现问题，已记录异常日志");
        }
    }

    @Override
    public void saveFailure(String traceId, String toolName, String requestJson, String errorCode, String errorMessage, Long tenantId) {
        ToolCallLogEntity log = new ToolCallLogEntity();
        log.setTraceId(traceId);
        log.setToolName(toolName);
        log.setRequestJson(requestJson);
        log.setSuccess(0);
        log.setErrorCode(errorCode);
        log.setErrorMessage(errorMessage);
        log.setTenantId(tenantId);
        toolCallLogMapper.insert(log);
    }
}
