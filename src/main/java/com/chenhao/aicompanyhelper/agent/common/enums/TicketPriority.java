package com.chenhao.aicompanyhelper.agent.common.enums;

public enum TicketPriority {
    P1, P2, P3;

    public static boolean isValid(String value) {
        for (TicketPriority priority : values()) {
            if (priority.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
