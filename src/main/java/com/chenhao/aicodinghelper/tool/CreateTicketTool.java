package com.chenhao.aicodinghelper.tool;

import com.chenhao.aicodinghelper.dto.CreateTicketDTO;
import com.chenhao.aicodinghelper.service.TicketService;
import com.chenhao.aicodinghelper.vo.TicketResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateTicketTool implements AgentTool<CreateTicketDTO, TicketResultVO> {

    private final TicketService ticketService;

    @Override
    public String name() {
        return "create_ticket";
    }

    @Override
    public String description() {
        return "创建新工单，适用于故障上报、需求提交等场景。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("title", "description");
    }

    @Override
    public Map<String, String> parameterHints() {
        return Map.of(
                "title", "工单标题，建议10-30字",
                "description", "详细问题描述，包含现象/影响/复现信息",
                "priority", "优先级，允许值 P1/P2/P3，默认 P3"
        );
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
