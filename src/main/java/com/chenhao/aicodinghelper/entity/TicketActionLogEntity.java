package com.chenhao.aicodinghelper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket_action_log")
public class TicketActionLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ticketId;
    private String actionType;
    private String actionDetail;
    private Long operatorId;
    private LocalDateTime createdAt;
}
