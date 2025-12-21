# Panduan Deployment Inventaris MSU dengan Docker

Panduan ini langkah-langkah untuk menjalankan aplikasi **Inventaris MSU** menggunakan Docker Compose. Pastikan Anda sudah menginstall **Docker Desktop** atau **Docker Engine** dan **Git** sebelum memulai.

## 1. Persiapan

1.  **Clone Repository**
    Buka terminal dan clone repository ini:
    ```bash
    git clone https://github.com/Kruwpuck/Inventaris-MSU-PBO.git
    cd Inventaris-MSU-PBO/InventarisMSU
    ```

2.  **Struktur Docker**
    Aplikasi ini terdiri dari 4 layanan (service) yang akan berjalan secara bersamaan:
    *   **inventaris-app**: Aplikasi Backend & Frontend (Spring Boot) berjalan di port internal 8080.
    *   **inventaris-db**: Database MySQL 8.0.
    *   **inventaris-phpmyadmin**: Interface database visual (akses via port 8081).
    *   **inventaris-nginx**: Reverse proxy yang mengekspos aplikasi di port 80.

## 2. Menjalankan Aplikasi

Jalankan perintah berikut di dalam folder `InventarisMSU` (di mana file `compose.yaml` berada):

```bash
docker compose up -d --build
```

**Penjelasan Perintah:**
*   `up`: Menjalankan container.
*   `-d`: Detached mode (berjalan di background).
*   `--build`: Memaksa build ulang image aplikasi agar perubahan kode terbaru diterapkan.

Tunggu hingga proses build selesai. Ini mungkin memakan waktu beberapa menit tergantung kecepatan internet (untuk mengunduh dependencies Maven & Image Docker).

## 3. Akses Aplikasi

Setelah container berjalan, Anda dapat mengakses:

| Layanan | URL | Keterangan |
| :--- | :--- | :--- |
| **Website Utama** | [http://localhost](http://localhost) | Portal Peminjaman (Guest & Admin) |
| **phpMyAdmin** | [http://localhost:8081](http://localhost:8081) | Manajemen Database MySQL |
| **API Endpoint** | [http://localhost:8080](http://localhost:8080) | Akses langsung ke Spring Boot (Opsional) |

## 4. Troubleshooting

*   **Cek Status Container**:
    ```bash
    docker ps
    ```
    Pastikan semua container berstatus `Up`.

*   **Melihat Logs**:
    Jika ada error, cek logs aplikasi:
    ```bash
    docker logs -f inventaris-app
    ```

*   **Menghentikan Aplikasi**:
    ```bash
    docker compose down
    ```

## 5. Konfigurasi Tambahan

File konfigurasi utama terletak di `src/main/resources/application.yaml`. Anda dapat mengubah konfigurasi database atau email di sana. Jika mengubah file ini, jangan lupa jalankan `docker compose up -d --build` lagi.
