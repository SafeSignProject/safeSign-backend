package com.safesign.backend.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class KstTime {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private KstTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(KST);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(KST);
    }
}
