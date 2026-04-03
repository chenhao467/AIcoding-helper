package com.chenhao.aicodinghelper.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tool_call_log")
public class ToolCallLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String traceId;
    private String toolName;
    private String requestJson;
    private String responseJson;
    private Integer success;
    private String errorCode;
    private String errorMessage;
    private Long ticketId;
    private Long tenantId;
    private LocalDateTime createdAt;
}
