# Catatan Keuangan

Aplikasi Android sederhana untuk mencatat pemasukan dan pengeluaran. Java + XML tanpa library tambahan.
Data disimpan lokal di HP (SQLite), tidak dikirim ke internet.

## Fitur
- Tambah, ubah, dan hapus transaksi (pemasukan atau pengeluaran)
- Nominal otomatis berformat rupiah saat diketik
- Kategori, tanggal, dan catatan
- Saldo total, serta total pemasukan dan pengeluaran per bulan
- Geser antar bulan dengan tombol panah

## Build
Push ke GitHub. Tab Actions -> "Build APK" jalan otomatis.
Unduh artifact `CatatanKeuangan-debug-apk`, ekstrak, pasang `app-debug.apk`.

## Penting
- `app/debug.keystore` dikomit sengaja supaya semua build bertanda tangan sama.
  Dengan begitu versi baru bisa dipasang MENIMPA yang lama tanpa menghapus data.
  Jangan hapus file ini. Simpan repo sebagai Private.
- Kalau APK dibuat dengan keystore lain, Android menolak update dan kamu harus
  uninstall dulu, yang menghapus semua catatan.
