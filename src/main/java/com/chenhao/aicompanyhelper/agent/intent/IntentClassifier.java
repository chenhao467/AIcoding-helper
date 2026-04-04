//package com.chenhao.aicodinghelper.intent;
//
//import com.chenhao.aicodinghelper.common.enums.IntentType;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//public class IntentClassifier {
//
//    private static final Pattern TICKET_NO_PATTERN = Pattern.compile("(?i)T\\d{8,}");
//
//    /**
//     * MVP 阶段使用轻量规则分类，后续可替换为 LLM 意图分类器。
//     */
//    public IntentResult classify(String message) {
//        String lower = message.toLowerCase();
//        Map<String, String> slots = new HashMap<>();
//        Matcher matcher = TICKET_NO_PATTERN.matcher(message);
//        if (matcher.find()) {
//            slots.put("ticketNo", matcher.group().toUpperCase());
//        }
//
//        if (lower.contains("创建工单") || lower.contains("提单") || lower.contains("create ticket")) {
//            slots.put("priority", extractPriority(message));
//            return IntentResult.builder().intentType(IntentType.CREATE_TICKET).slots(slots).build();
//        }
//        if (lower.contains("查询工单") || lower.contains("进度") || lower.contains("查一下") || slots.containsKey("ticketNo")) {
//            return IntentResult.builder().intentType(IntentType.QUERY_TICKET).slots(slots).build();
//        }
//        if (lower.contains("关闭工单") || lower.contains("更新状态") || lower.contains("改成") || lower.contains("处理中")) {
//            slots.put("targetStatus", extractStatus(message));
//            return IntentResult.builder().intentType(IntentType.UPDATE_TICKET_STATUS).slots(slots).build();
//        }
//        if (lower.contains("sop") || lower.contains("502") || lower.contains("知识") || lower.contains("怎么处理")) {
//            return IntentResult.builder().intentType(IntentType.KNOWLEDGE_QA).slots(slots).build();
//        }
//
//        return IntentResult.builder().intentType(IntentType.GENERAL_QA).slots(slots).build();
//    }
//
//    private String extractPriority(String message) {
//        if (message.contains("高") || message.toUpperCase().contains("P1")) {
//            return "P1";
//        }
//        if (message.contains("中") || message.toUpperCase().contains("P2")) {
//            return "P2";
//        }
//        return "P3";
//    }
//
//    private String extractStatus(String message) {
//        if (message.contains("关闭")) {
//            return "CLOSED";
//        }
//        if (message.contains("解决")) {
//            return "RESOLVED";
//        }
//        if (message.contains("处理中")) {
//            return "PROCESSING";
//        }
//        return "OPEN";
//    }
//}
