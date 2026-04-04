package com.chenhao.aicompanyhelper.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_user")
public class AgentUserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userNo;
    private String userName;
    private String roleCode;
    private Long tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
