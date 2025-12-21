# Dokumentasi Alur Website Inventaris MSU

Websita ini dirancang untuk memfasilitasi peminjaman barang dan ruangan di Masjid Syamsul Ulum (MSU) secara online. Berikut adalah alur penggunaan aplikasi untuk **Guest (Peminjam)** dan **Pengelola (Admin)**.

## A. Alur Peminjam (Guest)

1.  **Halaman Utama (Landing Page)**
    *   Akses: `http://localhost/`
    *   Pengguna melihat informasi umum, item unggulan, dan navigasi.

2.  **Katalog & Pencarian**
    *   Halaman Barang: `http://localhost/catalogue`
    *   Halaman Ruangan: `http://localhost/ruangan`
    *   Pengguna dapat mencari barang/ruangan, memfilter berdasarkan tanggal/waktu untuk melihat ketersediaan stok secara *real-time*.

3.  **Proses Booking (Keranjang)**
    *   Pengguna memilih item dengan tombol `+`. Item masuk ke keranjang belanja (floating cart).
    *   Klik tombol Checkout (ikon tas) untuk melanjutkan.

4.  **Formulir Peminjaman**
    *   Akses: `http://localhost/form`
    *   Pengguna mengisi data diri lengkap:
        *   Nama, Email, No HP, NIM/NIP.
        *   Unit/Departemen.
        *   Tujuan Peminjaman.
        *   **Identitas (KTM/KTP)**: Wajib upload file gambar/PDF.
        *   **Dokumen Pendukung**: Surat permohonan (opsional/wajib sesuai kebutuhan).
    *   Sistem memvalidasi durasi dan stok sebelum submit.

5.  **Submit & Konfirmasi**
    *   Setelah klik "Kirim Permohonan", data terkirim ke server.
    *   **Email Konfirmasi**: Peminjam otomatis menerima email konfirmasi bahwa permohonan telah diterima (Status: *PENDING*).

---

## B. Alur Pengelola (Admin Approval)

1.  **Login**
    *   Akses: `http://localhost/login`
    *   Masuk sebagai **Pengelola**.

2.  **Persetujuan (Approval)**
    *   Akses: Menu "Daftar Permohonan".
    *   Pengelola memverifikasi permohonan yang masuk.
    *   **Aksi**:
        *   **Setuju (Approve)**: Stok barang dibooking. Status berubah menjadi *APPROVED*. Permohonan lanjut ke tahap penyerahan.
        *   **Tolak (Reject)**: Pengelola wajib mengisi alasan. Peminjam mendapat notifikasi penolakan via email. Status berubah menjadi *REJECTED* dan proses selesai.

---

## C. Alur Pengurus (Penyerahan & Pengembalian)

Setelah permohonan disetujui (Approved), proses berlanjut ke **Pengurus** yang bertugas di lapangan.

1.  **Daftar Peminjaman Aktif**
    *   Akses: Menu "Peminjaman Ruangan" / "Fasilitas".
    *   Pengurus melihat daftar peminjam yang akan mengambil barang/ruangan hari ini.

2.  **Penyerahan Barang (Checklist Ambil)**
    *   Saat peminjam datang, Pengurus memverifikasi identitas.
    *   Pengurus mencentang **"Sudah Ambil"**.
    *   Status sistem berubah menjadi *TAKEN*.

3.  **Pengembalian Barang (Checklist Kembali)**
    *   Saat peminjam mengembalikan barang, Pengurus memeriksa kondisi barang.
    *   Pengurus mencentang **"Sudah Kembali"**.
    *   Status sistem berubah menjadi *COMPLETED* (Selesai).
    *   Stok barang otomatis kembali tersedia untuk peminjam lain.
    *   Data peminjaman berpindah ke menu **Riwayat**.
