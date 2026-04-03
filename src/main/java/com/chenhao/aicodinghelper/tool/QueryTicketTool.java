package com.chenhao.aicodinghelper.tool;

import com.chenhao.aicodinghelper.service.TicketService;
import com.chenhao.aicodinghelper.vo.TicketDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryTicketTool implements AgentTool<String, TicketDetailVO> {

    private final TicketService ticketService;

    @Override
    public String name() {
        return "query_ticket";
    }

    @Override
    public Class<String> inputType() {
        return String.class;
    }

    @Override
    public TicketDetailVO execute(String ticketNo, ToolContext context) {
        return ticketService.queryByTicketNo(ticketNo, context.getTenantId());
    }
}
