package com.chenhao.aicompanyhelper.agent.service;

import com.chenhao.aicompanyhelper.agent.dto.CreateTicketDTO;
import com.chenhao.aicompanyhelper.agent.dto.UpdateTicketStatusDTO;
import com.chenhao.aicompanyhelper.agent.vo.TicketDetailVO;
import com.chenhao.aicompanyhelper.agent.vo.TicketResultVO;

public interface TicketService {
    TicketResultVO createTicket(CreateTicketDTO dto);

    TicketDetailVO queryByTicketNo(String ticketNo, Long tenantId);

    TicketResultVO updateStatus(UpdateTicketStatusDTO dto);
}
