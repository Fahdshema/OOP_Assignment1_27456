package com.auca.interfaces;

import com.auca.exceptions.ValidationException;
import java.math.BigDecimal;

public interface Validatable {
    // interface constants (public static final)
    BigDecimal MAX_MONEY = new BigDecimal("999999999999.99");
    String DATE_FORMAT = "YYYY-MM-DD";

    void validate() throws ValidationException;

    void printDetails();

    String getSummary();

    // ---- small shared checking helpers used by all classes ----
    static boolean textOk(String s, int min, int max) {
        if (s == null) return false;
        int len = s.trim().length();
        return len >= min && len <= max;
    }

    static boolean digitsOk(String s, int exact) {
        return s != null && s.matches("\\d{" + exact + "}");
    }

    static boolean emailOk(String s) {
        return s != null && s.length() <= 120
                && s.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    }

    static boolean phoneOk(String s) {
        return s != null && s.matches("\\+?\\d{10,15}");
    }

    static boolean moneyOk(BigDecimal m) {
        return m != null && m.signum() >= 0 && m.compareTo(MAX_MONEY) <= 0 && m.scale() <= 2;
    }

    static boolean inList(String s, String... allowed) {
        if (s == null) return false;
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i].equals(s)) return true;
        }
        return false;
    }
}
