package com.irfandev.catatankeuangan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String[] EXPENSE_CATEGORIES = {
            "Makan & Minum", "Transportasi", "Belanja", "Tagihan", "Kesehatan",
            "Hiburan", "Pendidikan", "Lainnya"
    };
    private static final String[] INCOME_CATEGORIES = {
            "Gaji", "Bonus", "Usaha", "Hadiah", "Lainnya"
    };

    private final SimpleDateFormat monthKey = new SimpleDateFormat("yyyy-MM", Locale.US);
    private final SimpleDateFormat monthTitle = new SimpleDateFormat("MMMM yyyy", Fmt.ID);
    /** Bulan yang sedang ditampilkan, selalu tanggal 1. */
    private final Calendar month = Calendar.getInstance();

    private Db db;
    private TransactionAdapter adapter;
    private TextView tvBalance;
    private TextView tvMonth;
    private TextView tvIncome;
    private TextView tvExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new Db(this);
        month.set(Calendar.DAY_OF_MONTH, 1);

        tvBalance = findViewById(R.id.tvBalance);
        tvMonth = findViewById(R.id.tvMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);

        ListView list = findViewById(R.id.list);
        adapter = new TransactionAdapter(this);
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.empty));
        list.setOnItemClickListener((parent, view, position, id) -> showEditor(adapter.getItem(position)));

        findViewById(R.id.btnPrev).setOnClickListener(v -> shiftMonth(-1));
        findViewById(R.id.btnNext).setOnClickListener(v -> shiftMonth(1));
        findViewById(R.id.btnAdd).setOnClickListener(v -> showEditor(null));

        refresh();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void shiftMonth(int delta) {
        month.add(Calendar.MONTH, delta);
        refresh();
    }

    private void refresh() {
        String ym = monthKey.format(month.getTime());
        List<Transaction> items = db.byMonth(ym);
        adapter.setItems(items);

        tvMonth.setText(monthTitle.format(month.getTime()));
        tvBalance.setText(Fmt.rupiah(db.sum(Transaction.INCOME, null) - db.sum(Transaction.EXPENSE, null)));
        tvIncome.setText(Fmt.rupiah(db.sum(Transaction.INCOME, ym)));
        tvExpense.setText(Fmt.rupiah(db.sum(Transaction.EXPENSE, ym)));
    }

    /** Dialog tambah (existing == null) atau ubah/hapus transaksi. */
    private void showEditor(final Transaction existing) {
        View view = getLayoutInflater().inflate(R.layout.dialog_transaction, null);
        final RadioGroup rgType = view.findViewById(R.id.rgType);
        final EditText etAmount = view.findViewById(R.id.etAmount);
        final Spinner spCategory = view.findViewById(R.id.spCategory);
        final Button btnDate = view.findViewById(R.id.btnDate);
        final EditText etNote = view.findViewById(R.id.etNote);
        final String[] date = {existing != null ? existing.date : Fmt.today()};

        etAmount.addTextChangedListener(new MoneyWatcher());

        rgType.setOnCheckedChangeListener((group, checkedId) -> bindCategories(
                spCategory, checkedId == R.id.rbIncome ? Transaction.INCOME : Transaction.EXPENSE, null));

        if (existing != null) {
            rgType.check(existing.type == Transaction.INCOME ? R.id.rbIncome : R.id.rbExpense);
            etAmount.setText(Fmt.group(existing.amount));
            etNote.setText(existing.note);
            bindCategories(spCategory, existing.type, existing.category);
        } else {
            bindCategories(spCategory, Transaction.EXPENSE, null);
        }

        btnDate.setText(Fmt.prettyDate(date[0]));
        btnDate.setOnClickListener(x -> {
            int[] ymd = Fmt.parseDate(date[0]);
            new DatePickerDialog(this, (picker, y, m, d) -> {
                date[0] = Fmt.isoDate(y, m + 1, d);
                btnDate.setText(Fmt.prettyDate(date[0]));
            }, ymd[0], ymd[1] - 1, ymd[2]).show();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Tambah transaksi" : "Ubah transaksi")
                .setView(view)
                .setPositiveButton("Simpan", null)
                .setNegativeButton("Batal", null);
        if (existing != null) {
            builder.setNeutralButton("Hapus", null);
        }
        final AlertDialog dialog = builder.create();

        // Tombol diatur setelah dialog tampil supaya dialog tidak menutup saat input belum valid.
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                long amount = Fmt.digits(etAmount.getText().toString());
                if (amount <= 0) {
                    etAmount.setError("Isi nominal lebih dari 0");
                    return;
                }
                Transaction t = existing != null ? existing : new Transaction();
                t.type = rgType.getCheckedRadioButtonId() == R.id.rbIncome
                        ? Transaction.INCOME : Transaction.EXPENSE;
                t.amount = amount;
                t.category = (String) spCategory.getSelectedItem();
                t.note = etNote.getText().toString().trim();
                t.date = date[0];
                if (existing == null) {
                    db.insert(t);
                } else {
                    db.update(t);
                }
                dialog.dismiss();
                refresh();
            });
            if (existing != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                        .setOnClickListener(v -> confirmDelete(existing, dialog));
            }
        });

        if (existing == null && dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
    }

    private void confirmDelete(final Transaction t, final AlertDialog editor) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus transaksi?")
                .setMessage(t.category + " · " + Fmt.rupiah(t.amount))
                .setPositiveButton("Hapus", (d, which) -> {
                    db.delete(t.id);
                    editor.dismiss();
                    refresh();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void bindCategories(Spinner spinner, int type, String selected) {
        String[] cats = type == Transaction.INCOME ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cats);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(a);
        if (selected != null) {
            for (int i = 0; i < cats.length; i++) {
                if (cats[i].equals(selected)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    /** Menambah titik ribuan otomatis saat mengetik nominal: 1500000 menjadi 1.500.000. */
    private static final class MoneyWatcher implements TextWatcher {
        private boolean busy;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable e) {
            if (busy) {
                return;
            }
            busy = true;
            long value = Fmt.digits(e.toString());
            String formatted = value == 0 ? "" : Fmt.group(value);
            if (!formatted.equals(e.toString())) {
                e.replace(0, e.length(), formatted);
            }
            busy = false;
        }
    }
}
