package com.chenhao.aicodinghelper.service.impl;

import com.chenhao.aicodinghelper.entity.ToolCallLogEntity;
import com.chenhao.aicodinghelper.mapper.ToolCallLogMapper;
import com.chenhao.aicodinghelper.service.ToolCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolCallLogServiceImpl implements ToolCallLogService {

    private final ToolCallLogMapper toolCallLogMapper;

    @Override
    public void saveSuccess(String traceId, String toolName, String requestJson, String responseJson, Long ticketId, Long tenantId) {
        ToolCallLogEntity log = new ToolCallLogEntity();
        log.setTraceId(traceId);
        log.setToolName(toolName);
        log.setRequestJson(requestJson);
        log.setResponseJson(responseJson);
        log.setSuccess(1);
        log.setTicketId(ticketId);
        log.setTenantId(tenantId);
        toolCallLogMapper.insert(log);
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
