package com.irfandev.catatankeuangan;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Pemformat rupiah dan tanggal. */
final class Fmt {

    static final Locale ID = Locale.forLanguageTag("id-ID");

    private Fmt() {}

    /** 1500000 -> "1.500.000" */
    static String group(long value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ID);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("#,##0", symbols).format(value);
    }

    /** 1500000 -> "Rp 1.500.000", negatif -> "-Rp 1.500.000" */
    static String rupiah(long value) {
        return (value < 0 ? "-" : "") + "Rp " + group(Math.abs(value));
    }

    /** Ambil angka saja dari teks, maksimal 12 digit. Teks kosong jadi 0. */
    static long digits(String text) {
        String d = text.replaceAll("[^0-9]", "");
        if (d.isEmpty()) {
            return 0L;
        }
        if (d.length() > 12) {
            d = d.substring(0, 12);
        }
        return Long.parseLong(d);
    }

    static String isoDate(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    static String today() {
        Calendar c = Calendar.getInstance();
        return isoDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    /** "2026-10-12" -> {2026, 10, 12}. Kalau format salah, pakai hari ini. */
    static int[] parseDate(String iso) {
        try {
            String[] p = iso.split("-");
            return new int[] {Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])};
        } catch (RuntimeException e) {
            Calendar c = Calendar.getInstance();
            return new int[] {c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)};
        }
    }

    /** "2026-10-12" -> "12 Okt 2026" */
    static String prettyDate(String iso) {
        int[] p = parseDate(iso);
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(p[0], p[1] - 1, p[2]);
        return new SimpleDateFormat("d MMM yyyy", ID).format(c.getTime());
    }
}
