package com.chenhao.aicodinghelper.agent.service;

import com.chenhao.aicodinghelper.agent.dto.CreateTicketDTO;
import com.chenhao.aicodinghelper.agent.dto.UpdateTicketStatusDTO;
import com.chenhao.aicodinghelper.agent.vo.TicketDetailVO;
import com.chenhao.aicodinghelper.agent.vo.TicketResultVO;

public interface TicketService {
    TicketResultVO createTicket(CreateTicketDTO dto);

    TicketDetailVO queryByTicketNo(String ticketNo, Long tenantId);

    TicketResultVO updateStatus(UpdateTicketStatusDTO dto);
}
