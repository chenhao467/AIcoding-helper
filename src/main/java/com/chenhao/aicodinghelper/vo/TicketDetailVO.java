package com.chenhao.aicodinghelper.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketDetailVO {
    private String ticketNo;
    private String title;
    private String description;
    private String priority;
    private String status;
    private Long reporterId;
    private Long assigneeId;
    private LocalDateTime updatedAt;
}
