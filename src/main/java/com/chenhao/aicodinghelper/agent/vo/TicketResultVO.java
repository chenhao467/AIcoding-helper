package com.chenhao.aicodinghelper.agent.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketResultVO {
    private String ticketNo;
    private String status;
    private String message;
}
