package com.qonak.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Pure-Java helpers for prices, dates and Russian plurals (covered by unit tests). */
public final class Formatters {

    public static final String DATE_PATTERN = "dd.MM.yyyy";

    private Formatters() {
    }

    /** 85000.0 → "85 000 ₸" */
    public static String price(double amount) {
        long rounded = Math.round(amount);
        String digits = String.valueOf(Math.abs(rounded));
        StringBuilder sb = new StringBuilder();
        int firstGroup = digits.length() % 3;
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && (i - firstGroup) % 3 == 0) {
                sb.append(' '); // non-breaking space
            }
            sb.append(digits.charAt(i));
        }
        return (rounded < 0 ? "-" : "") + sb + " ₸";
    }

    /**
     * Picks the correct Russian word form for a number.
     * plural(1, "ночь", "ночи", "ночей") → "ночь", plural(5, …) → "ночей".
     */
    public static String plural(int n, String one, String few, String many) {
        int mod100 = Math.abs(n) % 100;
        int mod10 = mod100 % 10;
        if (mod100 >= 11 && mod100 <= 14) return many;
        if (mod10 == 1) return one;
        if (mod10 >= 2 && mod10 <= 4) return few;
        return many;
    }

    /** "3 ночи", "5 ночей" */
    public static String nights(int n) {
        return n + " " + plural(n, "ночь", "ночи", "ночей");
    }

    /** "1 гость", "2 гостя", "5 гостей" */
    public static String guests(int n) {
        return n + " " + plural(n, "гость", "гостя", "гостей");
    }

    public static String hotels(int n) {
        return n + " " + plural(n, "отель", "отеля", "отелей");
    }

    public static String restaurants(int n) {
        return n + " " + plural(n, "ресторан", "ресторана", "ресторанов");
    }

    public static SimpleDateFormat dateFormat() {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        format.setLenient(false);
        return format;
    }

    /** Parses "dd.MM.yyyy"; returns null for invalid input. */
    public static Date parseDate(String value) {
        if (value == null) return null;
        try {
            return dateFormat().parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /** Number of nights between two "dd.MM.yyyy" dates; 0 if invalid or not in order. */
    public static int nightsBetween(String checkIn, String checkOut) {
        Date in = parseDate(checkIn);
        Date out = parseDate(checkOut);
        if (in == null || out == null) return 0;
        long days = TimeUnit.MILLISECONDS.toDays(out.getTime() - in.getTime() + TimeUnit.HOURS.toMillis(12));
        return days > 0 ? (int) days : 0;
    }

    /** True if {@code date} is strictly after {@code other} (both "dd.MM.yyyy"). */
    public static boolean isAfter(String date, String other) {
        Date a = parseDate(date);
        Date b = parseDate(other);
        return a != null && b != null && a.after(b);
    }
}
