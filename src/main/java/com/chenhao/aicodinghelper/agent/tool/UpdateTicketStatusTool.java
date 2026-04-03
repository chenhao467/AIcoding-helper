package com.chenhao.aicodinghelper.agent.tool;

import com.chenhao.aicodinghelper.agent.dto.UpdateTicketStatusDTO;
import com.chenhao.aicodinghelper.agent.service.TicketService;
import com.chenhao.aicodinghelper.agent.vo.TicketResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpdateTicketStatusTool implements AgentTool<UpdateTicketStatusDTO, TicketResultVO> {

    private final TicketService ticketService;

    @Override
    public String name() {
        return "update_ticket_status";
    }

    @Override
    public String description() {
        return "更新工单状态，例如 OPEN/PROCESSING/RESOLVED/CLOSED。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("ticketNo", "targetStatus");
    }

    @Override
    public Map<String, String> parameterHints() {
        return Map.of(
                "ticketNo", "工单号",
                "targetStatus", "目标状态，允许值 OPEN/PROCESSING/RESOLVED/CLOSED"
        );
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
