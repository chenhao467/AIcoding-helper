package com.chenhao.aicodinghelper.tool;

import com.chenhao.aicodinghelper.dto.UpdateTicketStatusDTO;
import com.chenhao.aicodinghelper.service.TicketService;
import com.chenhao.aicodinghelper.vo.TicketResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateTicketStatusTool implements AgentTool<UpdateTicketStatusDTO, TicketResultVO> {

    private final TicketService ticketService;

    @Override
    public String name() {
        return "update_ticket_status";
    }

    @Override
    public Class<UpdateTicketStatusDTO> inputType() {
        return UpdateTicketStatusDTO.class;
    }

    @Override
    public TicketResultVO execute(UpdateTicketStatusDTO input, ToolContext context) {
        input.setOperatorId(context.getUserId());
        input.setTenantId(context.getTenantId());
        return ticketService.updateStatus(input);
    }
}
