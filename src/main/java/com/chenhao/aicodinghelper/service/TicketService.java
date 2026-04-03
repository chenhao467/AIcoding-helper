package com.chenhao.aicodinghelper.service;

import com.chenhao.aicodinghelper.dto.CreateTicketDTO;
import com.chenhao.aicodinghelper.dto.UpdateTicketStatusDTO;
import com.chenhao.aicodinghelper.vo.TicketDetailVO;
import com.chenhao.aicodinghelper.vo.TicketResultVO;

public interface TicketService {
    TicketResultVO createTicket(CreateTicketDTO dto);

    TicketDetailVO queryByTicketNo(String ticketNo, Long tenantId);

    TicketResultVO updateStatus(UpdateTicketStatusDTO dto);
}
