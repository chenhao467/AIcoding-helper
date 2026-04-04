package com.chenhao.aicompanyhelper.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_article")
public class KbArticleEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String kbNo;
    private String title;
    private String content;
    private String category;
    private String tags;
    private String status;
    private Long tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
