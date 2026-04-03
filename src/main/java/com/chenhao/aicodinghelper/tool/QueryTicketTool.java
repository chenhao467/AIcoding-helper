package com.chenhao.aicodinghelper.tool;

import com.chenhao.aicodinghelper.service.TicketService;
import com.chenhao.aicodinghelper.vo.TicketDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryTicketTool implements AgentTool<String, TicketDetailVO> {

    private final TicketService ticketService;

    @Override
    public String name() {
        return "query_ticket";
    }

    @Override
    public String description() {
        return "按工单号查询工单详情与进度状态。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("ticketNo");
    }

    @Override
    public Map<String, String> parameterHints() {
        return Map.of("ticketNo", "工单号，格式示例 T20260403001");
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
