package com.qonak.app.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FormattersTest {

    private static final String NBSP = " ";

    @Test
    public void priceIsGroupedByThousands() {
        assertEquals("85" + NBSP + "000" + NBSP + "₸", Formatters.price(85000.0));
        assertEquals("1" + NBSP + "250" + NBSP + "000" + NBSP + "₸", Formatters.price(1_250_000));
        assertEquals("900" + NBSP + "₸", Formatters.price(900));
        assertEquals("0" + NBSP + "₸", Formatters.price(0));
    }

    @Test
    public void russianPluralsFollowGrammarRules() {
        assertEquals("1 ночь", Formatters.nights(1));
        assertEquals("3 ночи", Formatters.nights(3));
        assertEquals("5 ночей", Formatters.nights(5));
        assertEquals("11 ночей", Formatters.nights(11));
        assertEquals("21 ночь", Formatters.nights(21));
        assertEquals("22 гостя", Formatters.guests(22));
        assertEquals("112 отелей", Formatters.hotels(112));
    }

    @Test
    public void nightsAreCountedAcrossMonthsAndYears() {
        assertEquals(1, Formatters.nightsBetween("04.06.2026", "05.06.2026"));
        assertEquals(3, Formatters.nightsBetween("30.11.2026", "03.12.2026"));
        assertEquals(2, Formatters.nightsBetween("31.12.2026", "02.01.2027"));
        assertEquals(0, Formatters.nightsBetween("05.06.2026", "04.06.2026"));
        assertEquals(0, Formatters.nightsBetween("not a date", "04.06.2026"));
    }

    @Test
    public void datesAreComparedAsDatesNotStrings() {
        // As strings "01.12.2026" < "15.11.2026", which was the original bug.
        assertTrue(Formatters.isAfter("01.12.2026", "15.11.2026"));
        assertFalse(Formatters.isAfter("15.11.2026", "01.12.2026"));
        assertFalse(Formatters.isAfter("15.11.2026", "15.11.2026"));
    }

    @Test
    public void invalidDatesAreRejected() {
        assertNull(Formatters.parseDate("31.02.2026"));
        assertNull(Formatters.parseDate(null));
    }
}
