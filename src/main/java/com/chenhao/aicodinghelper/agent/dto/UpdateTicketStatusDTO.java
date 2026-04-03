package com.chenhao.aicodinghelper.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTicketStatusDTO {
    @NotBlank
    private String ticketNo;
    @NotBlank
    private String targetStatus;
    @NotNull
    private Long operatorId;
    @NotNull
    private Long tenantId;
}
