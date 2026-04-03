package com.chenhao.aicodinghelper.common.enums;

public enum TicketStatus {
    OPEN,
    PROCESSING,
    RESOLVED,
    CLOSED;

    public static boolean isValid(String value) {
        for (TicketStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
