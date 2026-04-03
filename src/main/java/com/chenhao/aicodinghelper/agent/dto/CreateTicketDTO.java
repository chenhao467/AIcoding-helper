package com.chenhao.aicodinghelper.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateTicketDTO {
    @NotBlank
    private String title;

    @NotBlank
    @Pattern(regexp = "P1|P2|P3", message = "priority must be one of P1/P2/P3")
    private String priority;

    @NotBlank
    private String description;

    @NotNull
    private Long reporterId;

    @NotNull
    private Long tenantId;
}
