package com.irfandev.catatankeuangan;

/** Satu catatan keuangan. Nominal disimpan dalam rupiah bulat. */
final class Transaction {
    static final int INCOME = 0;
    static final int EXPENSE = 1;

    long id;
    int type = EXPENSE;
    long amount;
    String category = "";
    String note = "";
    /** Format yyyy-MM-dd. */
    String date = "";
}
