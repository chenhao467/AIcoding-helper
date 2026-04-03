package com.chenhao.aicodinghelper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenhao.aicodinghelper.common.enums.TicketPriority;
import com.chenhao.aicodinghelper.common.enums.TicketStatus;
import com.chenhao.aicodinghelper.common.exception.BizException;
import com.chenhao.aicodinghelper.dto.CreateTicketDTO;
import com.chenhao.aicodinghelper.dto.UpdateTicketStatusDTO;
import com.chenhao.aicodinghelper.entity.TicketActionLogEntity;
import com.chenhao.aicodinghelper.entity.TicketEntity;
import com.chenhao.aicodinghelper.mapper.TicketActionLogMapper;
import com.chenhao.aicodinghelper.mapper.TicketMapper;
import com.chenhao.aicodinghelper.service.TicketService;
import com.chenhao.aicodinghelper.vo.TicketDetailVO;
import com.chenhao.aicodinghelper.vo.TicketResultVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final TicketMapper ticketMapper;
    private final TicketActionLogMapper actionLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketResultVO createTicket(CreateTicketDTO dto) {
        if (!TicketPriority.isValid(dto.getPriority())) {
            throw new BizException("优先级不合法，仅支持 P1/P2/P3");
        }

        TicketEntity entity = new TicketEntity();
        entity.setTicketNo(generateTicketNo());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPriority(dto.getPriority().toUpperCase());
        entity.setStatus(TicketStatus.OPEN.name());
        entity.setReporterId(dto.getReporterId());
        entity.setSource("AI_AGENT");
        entity.setTenantId(dto.getTenantId());
        ticketMapper.insert(entity);

        saveActionLog(entity.getId(), "CREATE", "AI 创建工单", dto.getReporterId());
        return new TicketResultVO(entity.getTicketNo(), entity.getStatus(), "工单创建成功");
    }

    @Override
    public TicketDetailVO queryByTicketNo(String ticketNo, Long tenantId) {
        TicketEntity entity = ticketMapper.selectOne(new LambdaQueryWrapper<TicketEntity>()
                .eq(TicketEntity::getTicketNo, ticketNo)
                .eq(TicketEntity::getTenantId, tenantId)
                .last("limit 1"));

        if (entity == null) {
            throw new BizException("工单不存在: " + ticketNo);
        }

        return TicketDetailVO.builder()
                .ticketNo(entity.getTicketNo())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .reporterId(entity.getReporterId())
                .assigneeId(entity.getAssigneeId())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketResultVO updateStatus(UpdateTicketStatusDTO dto) {
        if (!TicketStatus.isValid(dto.getTargetStatus())) {
            throw new BizException("目标状态不合法，仅支持 OPEN/PROCESSING/RESOLVED/CLOSED");
        }

        TicketEntity entity = ticketMapper.selectOne(new LambdaQueryWrapper<TicketEntity>()
                .eq(TicketEntity::getTicketNo, dto.getTicketNo())
                .eq(TicketEntity::getTenantId, dto.getTenantId())
                .last("limit 1"));

        if (entity == null) {
            throw new BizException("工单不存在: " + dto.getTicketNo());
        }

        entity.setStatus(dto.getTargetStatus().toUpperCase());
        ticketMapper.updateById(entity);
        saveActionLog(entity.getId(), "STATUS_CHANGE", "状态变更为: " + entity.getStatus(), dto.getOperatorId());
        return new TicketResultVO(entity.getTicketNo(), entity.getStatus(), "状态更新成功");
    }

    private void saveActionLog(Long ticketId, String actionType, String detail, Long operatorId) {
        TicketActionLogEntity actionLog = new TicketActionLogEntity();
        actionLog.setTicketId(ticketId);
        actionLog.setActionType(actionType);
        ObjectMapper mapper = new ObjectMapper();
        try {
            actionLog.setActionDetail(mapper.writeValueAsString("Invalid value."));
        }catch (JsonProcessingException e){
            log.info("保存工单详细信息时json格式解析错误");
        }
        actionLog.setOperatorId(operatorId);
        actionLogMapper.insert(actionLog);
    }

    private String generateTicketNo() {
        // 采用日期+随机码，MVP 阶段足够，生产可改为号段服务。
        return "T" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
