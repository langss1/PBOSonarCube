# Dokumentasi API & Backend (Sederhana)

Dokumen ini menjelaskan struktur backend aplikasi Inventaris MSU agar mudah dipahami oleh pengembang baru.

## Teknologi Utama
*   **Bahasa**: Java 17
*   **Framework**: Spring Boot 3.x (Web, Data JPA, Validation, Mail)
*   **Database**: MySQL 8.0
*   **Frontend**: Thymeleaf (Server-side rendering) + Vanilla JS

## Arsitektur (MVC Pattern)

Aplikasi menggunakan pola **Model-View-Controller**:

### 1. Model (Data)
Lokasi: `com.Habb.InventarisMSU.model`
Merepresentasikan tabel di database.
*   **Peminjaman**: Menyimpan data transaksi peminjaman (Nama, Tanggal, Status).
    *   *Status Enum*: `PENDING`, `APPROVED`, `REJECTED`, `TAKEN`, `COMPLETED`, `CANCELLED`.
*   **Item**: Menyimpan data barang/ruangan (Nama, Stok, Gambar).
*   **PeminjamanDetail**: Tabel perantara (Many-to-Many) yang mencatat item apa saja dan berapa jumlahnya dalam satu peminjaman.

### 2. Repository (Akses Database & Keamanan SQL Injection)
Lokasi: `com.Habb.InventarisMSU.repository`
Menggunakan **Spring Data JPA**.

**Penanganan SQL Injection:**
Aplikasi aman dari SQL Injection karena menggunakan mekanisme enkapsulasi query dari JPA.

**1. Prepared Statements (Query Terkompilasi)**
*   Saat menggunakan derived query seperti `findByStatus` atau `findByType`, Hibernate secara otomatis membuat *Prepared Statement*.
*   **Mekanisme**: Database mengkompilasi struktur SQL terlebih dahulu (contoh: `SELECT * FROM items WHERE type = ?`). Input user dikirim secara terpisah sebagai parameter data, bukan digabung ke dalam string SQL.
*   **Hasil**: Input berbahaya seperti `' OR 1=1 --` akan dianggap sebagai literal string biasa, tidak akan mengubah logika query.

**2. Named Parameters (Query Manual)**
*   Untuk query kustom dengan `@Query`, digunakan binding parameter (`:param`).
*   **File Referensi**: `PeminjamanRepository.java` (Line 16-30).
*   *Contoh Kode*:
    ```java
    @Query("SELECT p FROM Peminjaman p WHERE p.startDate <= :endOfMonth")
    List<Peminjaman> findBookingsInDataRange(@Param("endOfMonth") LocalDate endOfMonth);
    ```
*   Parameter `:endOfMonth` menjamin input diproses aman oleh driver JDBC.

### 3. Service (Logika Bisnis)
Lokasi: `com.Habb.InventarisMSU.service`
Tempat "otak" aplikasi bekerja.
*   **GuestBookingService**:
    *   `submitBooking()`: Menerima data form, upload file, simpan ke database, dan kirim email.
    *   Validation: Mengandalkan tipe data kuat (Strong Typing) seperti `Integer` dan `MultipartFile` untuk mencegah input sampah.
    *   `checkAvailability()`: Logika mengecek stok. **Penting**: Stok hanya berkurang jika ada peminjaman lain di tanggal yang sama dengan status *APPROVED* atau *TAKEN*.
*   **PeminjamanService**: Digunakan oleh Admin untuk update status (Approve/Reject).
*   **EmailService**: Mengirim email via SMTP Gmail.

### 4. Controller (API Endpoints & Error Handling)
Lokasi: `com.Habb.InventarisMSU.controller`
Menangani request dari browser/frontend.

**Error Handling:**
Setiap endpoint kritis dilengkapi dengan blok `try-catch` untuk menangkap exception tak terduga dan mengembalikan respons HTTP yang sesuai (misal 500 Internal Server Error).
*   **File Referensi**: `PeminjamanController.java` (Line 64-67).
*   *Implementasi*:
    ```java
    try {
        guestBookingService.submitBooking(...);
        return ResponseEntity.ok(...);
    } catch (Exception e) {
        e.printStackTrace(); // Log error ke console server
        return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
    }
    ```

## Keamanan (Security)

Keamanan aplikasi dikelola oleh **Spring Security**.
**File Referensi Utama**: `config/SecurityConfig.java`.

### A. Authentication (Login)
Proses verifikasi identitas pengguna.
*   **Logic**: `DaoAuthenticationProvider` (Line 54) memverifikasi kredensial.
*   **User Service**: `CustomUserDetailsService.java` (Line 24-46) bertugas memuat data user dari database.
*   **Session**: Menggunakan `JSESSIONID` (HttpOnly cookie) untuk menjaga sesi pengguna setelah login.

### B. Authorization (Hak Akses)
Mengontrol siapa boleh mengakses apa menggunakan Role-Based Access Control (RBAC).
*   **Aturan Akses**: Didefinisikan dalam `securityFilterChain` (Line 31-38).
    *   `hasRole("PENGELOLA")`: Akses khusus admin pengelola.
    *   `hasRole("PENGURUS")`: Akses khusus pengurus lapangan.
    *   `permitAll()`: Akses publik untuk guest.

### C. Proteksi Data & Serangan
1.  **Hashing Password**:
    *   Menggunakan **BCrypt** (Line 67-69). Algoritma ini menambahkan *salt* acak secara otomatis untuk mencegah serangan *Rainbow Table*. Password asli tidak pernah disimpan.
2.  **XSS (Cross-Site Scripting)**:
    *   Output di frontend dilindungi oleh **Thymeleaf**. Secara default, Thymeleaf melakukan *escaping* pada semua variabel (`th:text`), mengubah karakter berbahaya (`<script>`) menjadi entity aman (`&lt;script&gt;`).
3.  **CSRF (Cross-Site Request Forgery)**:
    *   Dinonaktifkan sementara (`.csrf(csrf -> csrf.disable())`) untuk memudahkan interaksi API publik (peminjaman guest). Dalam produksi dengan form sensitif, ini bisa diaktifkan kembali.

## Konfigurasi Database (JDBC)
Aplikasi menggunakan koneksi JDBC standard ke MySQL.
*   **URL**: `jdbc:mysql://inventaris-db:3306/inventaris`
*   **Username**: `root`
*   **Password**: `rootpassword` (sesuai `compose.yaml`)
