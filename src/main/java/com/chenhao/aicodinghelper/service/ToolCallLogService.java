package com.chenhao.aicodinghelper.service;

public interface ToolCallLogService {
    void saveSuccess(String traceId, String toolName, String requestJson, String responseJson, Long ticketId, Long tenantId);

    void saveFailure(String traceId, String toolName, String requestJson, String errorCode, String errorMessage, Long tenantId);
}
