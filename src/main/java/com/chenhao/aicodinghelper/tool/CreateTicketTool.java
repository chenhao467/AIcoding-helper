package com.chenhao.aicodinghelper.tool;

import com.chenhao.aicodinghelper.dto.CreateTicketDTO;
import com.chenhao.aicodinghelper.service.TicketService;
import com.chenhao.aicodinghelper.vo.TicketResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTicketTool implements AgentTool<CreateTicketDTO, TicketResultVO> {

    private final TicketService ticketService;

    @Override
    public String name() {
        return "create_ticket";
    }

    @Override
    public Class<CreateTicketDTO> inputType() {
        return CreateTicketDTO.class;
    }

    @Override
    public TicketResultVO execute(CreateTicketDTO input, ToolContext context) {
        input.setReporterId(context.getUserId());
        input.setTenantId(context.getTenantId());
        return ticketService.createTicket(input);
    }
}
