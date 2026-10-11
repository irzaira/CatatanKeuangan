package com.irfandev.catatankeuangan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

final class TransactionAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private List<Transaction> items = new ArrayList<>();

    TransactionAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    void setItems(List<Transaction> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Transaction getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder h;
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.item_transaction, parent, false);
            h = new Holder();
            h.category = row.findViewById(R.id.tvCategory);
            h.sub = row.findViewById(R.id.tvSub);
            h.amount = row.findViewById(R.id.tvAmount);
            row.setTag(h);
        } else {
            h = (Holder) row.getTag();
        }

        Transaction t = items.get(position);
        boolean income = t.type == Transaction.INCOME;

        h.category.setText(t.category);
        String date = Fmt.prettyDate(t.date);
        h.sub.setText(t.note.isEmpty() ? date : date + " · " + t.note);
        h.amount.setText((income ? "+ " : "- ") + Fmt.rupiah(t.amount));
        h.amount.setTextColor(row.getContext().getColor(income ? R.color.income : R.color.expense));
        return row;
    }

    private static final class Holder {
        TextView category;
        TextView sub;
        TextView amount;
    }
}
