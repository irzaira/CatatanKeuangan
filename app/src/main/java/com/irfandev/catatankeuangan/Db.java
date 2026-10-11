package com.irfandev.catatankeuangan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Penyimpanan SQLite lokal. Data hanya ada di HP, tidak dikirim ke mana pun. */
final class Db extends SQLiteOpenHelper {

    private static final String NAME = "keuangan.db";
    private static final int VERSION = 1;
    private static final String TABLE = "transaksi";

    Db(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "type INTEGER NOT NULL, "
                + "amount INTEGER NOT NULL, "
                + "category TEXT NOT NULL, "
                + "note TEXT NOT NULL DEFAULT '', "
                + "date TEXT NOT NULL)");
        db.execSQL("CREATE INDEX idx_transaksi_date ON " + TABLE + "(date)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Belum ada perubahan skema.
    }

    private static ContentValues values(Transaction t) {
        ContentValues v = new ContentValues();
        v.put("type", t.type);
        v.put("amount", t.amount);
        v.put("category", t.category);
        v.put("note", t.note == null ? "" : t.note);
        v.put("date", t.date);
        return v;
    }

    long insert(Transaction t) {
        return getWritableDatabase().insert(TABLE, null, values(t));
    }

    void update(Transaction t) {
        getWritableDatabase().update(TABLE, values(t), "id = ?", new String[] {String.valueOf(t.id)});
    }

    void delete(long id) {
        getWritableDatabase().delete(TABLE, "id = ?", new String[] {String.valueOf(id)});
    }

    /** Transaksi pada satu bulan, terbaru di atas. yearMonth berformat yyyy-MM. */
    List<Transaction> byMonth(String yearMonth) {
        List<Transaction> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE, null, "date LIKE ?", new String[] {yearMonth + "-%"},
                null, null, "date DESC, id DESC")) {
            int iId = c.getColumnIndexOrThrow("id");
            int iType = c.getColumnIndexOrThrow("type");
            int iAmount = c.getColumnIndexOrThrow("amount");
            int iCategory = c.getColumnIndexOrThrow("category");
            int iNote = c.getColumnIndexOrThrow("note");
            int iDate = c.getColumnIndexOrThrow("date");
            while (c.moveToNext()) {
                Transaction t = new Transaction();
                t.id = c.getLong(iId);
                t.type = c.getInt(iType);
                t.amount = c.getLong(iAmount);
                t.category = c.getString(iCategory);
                String note = c.getString(iNote);
                t.note = note == null ? "" : note;
                t.date = c.getString(iDate);
                out.add(t);
            }
        }
        return out;
    }

    /** Total nominal satu jenis. yearMonth boleh null untuk semua waktu. */
    long sum(int type, String yearMonth) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM " + TABLE + " WHERE type = ?";
        String[] args;
        if (yearMonth == null) {
            args = new String[] {String.valueOf(type)};
        } else {
            sql += " AND date LIKE ?";
            args = new String[] {String.valueOf(type), yearMonth + "-%"};
        }
        try (Cursor c = getReadableDatabase().rawQuery(sql, args)) {
            return c.moveToFirst() ? c.getLong(0) : 0L;
        }
    }
}
