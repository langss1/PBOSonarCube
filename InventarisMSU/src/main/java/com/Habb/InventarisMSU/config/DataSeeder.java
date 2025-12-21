package com.Habb.InventarisMSU.config;

import com.Habb.InventarisMSU.model.Item;
import com.Habb.InventarisMSU.model.ItemType;
import com.Habb.InventarisMSU.model.Peminjaman;
import com.Habb.InventarisMSU.model.PeminjamanDetail;
import com.Habb.InventarisMSU.model.PeminjamanStatus;
import com.Habb.InventarisMSU.model.Role;
import com.Habb.InventarisMSU.model.User;
import com.Habb.InventarisMSU.repository.ItemRepository;
import com.Habb.InventarisMSU.repository.PeminjamanRepository;
import com.Habb.InventarisMSU.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private com.Habb.InventarisMSU.repository.LaporanRepository laporanRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            seedUsers();
            seedItems();
            seedPeminjaman(); // Integrating this back
            seedLaporan();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR IN DATA SEEDER: " + e.getMessage());
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            User pengelola = new User();
            pengelola.setEmail("pengelola@msu.com");
            pengelola.setPassword(passwordEncoder.encode("password"));
            pengelola.setRole(Role.PENGELOLA);
            userRepository.save(pengelola);

            User pengurus = new User();
            pengurus.setEmail("pengurus@msu.com");
            pengurus.setPassword(passwordEncoder.encode("password"));
            pengurus.setRole(Role.PENGURUS);
            userRepository.save(pengurus);
        }
    }

    private void seedItems() {
        // Barang
        createItemIfNotExists("Proyektor", ItemType.BARANG, 5, "proyektor.jpeg",
                "Proyektor berkualitas tinggi dan bagus");
        createItemIfNotExists("Sound System", ItemType.BARANG, 7, "sound.jpeg", "Sound system lengkap");
        createItemIfNotExists("Terpal", ItemType.BARANG, 16, "terpal.jpeg", "Terpal ukuran besar");
        createItemIfNotExists("Karpet", ItemType.BARANG, 20, "karpet.jpeg", "Karpet masjid");
        createItemIfNotExists("Kursi Lipat", ItemType.BARANG, 20, "kursi.jpeg", "Kursi lipat besi");
        createItemIfNotExists("Meja Lipat", ItemType.BARANG, 10, "meja.jpeg", "Meja lipat portable");
        createItemIfNotExists("Mic Wireless", ItemType.BARANG, 6, "mic.jpeg", "Microphone wireless");
        createItemIfNotExists("Kabel Roll", ItemType.BARANG, 9, "kabel.jpeg", "Kabel roll panjang");
        createItemIfNotExists("Tikar", ItemType.BARANG, 15, "tikar.jpeg", "Tikar plastik");
        createItemIfNotExists("Speaker Portable", ItemType.BARANG, 4, "speaker.jpeg",
                "Speaker portable dengan baterai");
        createItemIfNotExists("Speaker", ItemType.BARANG, 5, "speaker.jpeg", "Speaker standar");
        createItemIfNotExists("Meja Kayu", ItemType.BARANG, 10, "meja_kayu.jpeg", "Meja Kayu");
        createItemIfNotExists("Hijab", ItemType.BARANG, 20, "hijab.jpeg", "Pembatas Hijab");
        createItemIfNotExists("Sofa", ItemType.BARANG, 3, "sofa.jpeg", "Sofa Tamu");
        createItemIfNotExists("Akun Zoom MSU", ItemType.BARANG, 1, "zoom.png", "Akun Zoom Premium");
        createItemIfNotExists("Meja", ItemType.BARANG, 10, "meja_biasa.jpeg", "Meja Biasa");
        createItemIfNotExists("VIP4", ItemType.BARANG, 10, "c99ea443-e84b-4e32-bdb6-1fb98d0c4757.jpeg", "21");

        // Ruangan / Fasilitas
        createItemIfNotExists("Tidur", ItemType.RUANGAN, 12, "plaza.jpeg", "4");
        createItemIfNotExists("Aula Utama", ItemType.RUANGAN, 1, "plaza.jpeg",
                "Aula utama untuk kegiatan besar");
        createItemIfNotExists("Ruang Rapat A", ItemType.RUANGAN, 2, "plaza.jpeg", "Ruang rapat kecil A");
        createItemIfNotExists("Ruang Rapat B", ItemType.RUANGAN, 1, "plaza.jpeg", "Ruang rapat kecil B");
        createItemIfNotExists("Ruang Kajian", ItemType.RUANGAN, 1, "plaza.jpeg", "Ruang untuk kajian rutin");
        createItemIfNotExists("Ruang Tamu", ItemType.RUANGAN, 1, "plaza.jpeg", "Ruang tamu untuk penerimaan");
        createItemIfNotExists("Kelas 1", ItemType.RUANGAN, 1, "plaza.jpeg", "Ruang Kelas 1");
        createItemIfNotExists("Kelas 2", ItemType.RUANGAN, 1, "plaza.jpeg", "Ruang Kelas 2");
        createItemIfNotExists("Kelas 3", ItemType.RUANGAN, 1, "plaza.jpeg", "Ruang Kelas 3");
        createItemIfNotExists("Kelas 4", ItemType.RUANGAN, 1, "plaza.jpeg", "Ruang Kelas 4");
        createItemIfNotExists("Perpustakaan", ItemType.RUANGAN, 1, "plaza.jpeg", "Perpustakaan umum");
        createItemIfNotExists("Ruang Utama", ItemType.RUANGAN, 1, "ruang_utama.jpeg", "Ruang Utama Masjid");
        createItemIfNotExists("Pelataran Masjid", ItemType.RUANGAN, 1, "pelataran.jpeg", "Pelataran Masjid");
        createItemIfNotExists("Selasar Selatan", ItemType.RUANGAN, 1, "selasar.jpeg", "Selasar Selatan");
        createItemIfNotExists("Lantai 2 Timur", ItemType.RUANGAN, 1, "lantai2.jpeg", "Lantai 2 Timur");
        createItemIfNotExists("Ruang Tamu VIP", ItemType.RUANGAN, 1, "vip.jpeg", "Ruang Tamu VIP");
    }

    private void createItemIfNotExists(String name, ItemType type, int stock, String imageUrl, String description) {
        if (itemRepository.findByName(name) == null) {
            Item item = new Item();
            item.setName(name);
            item.setType(type);
            item.setStock(stock);
            item.setImageUrl(imageUrl);
            item.setDescription(description);
            item.setStatus("Tersedia");
            itemRepository.save(item);
        } else {
            // Optional: Update existing item to match exact specs if needed.
            // For now, simpler to just ensure existence or update if found.
            Item item = itemRepository.findByName(name);
            item.setStock(stock);
            // Don't overwrite image/desc unless necessary, but user wants 'sesuai
            // tampilan'.
            // But 'createItemIfNotExists' implies only create.
            // Check user request: "items jika di up ke temen saya maka akan sesuai"
            // So a friend receiving this will start from 0. createItemIfNotExists is fine.
            // But for CURRENT user, validation implies database is already right.
            // I will allow it to update stock just in case it runs again.
            item.setType(type);
            item.setImageUrl(imageUrl);
            item.setDescription(description);
            itemRepository.save(item);
        }
    }

    private void seedPeminjaman() {
        if (peminjamanRepository.count() == 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

            // 1. Proyektor / Barang / Ruslan Ismail / 10/01/2024 - 10/03/2024 / Sudah
            // Kembali (10/03/2024) / 2
            createLoan("Ruslan Ismail", "Proyektor", 2, "10/01/2024", "10/03/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter);

            // 2. Karpet / Barang / Buana Ahmad / 10/26/2024 - 11/02/2024 / Sedang Dipinjam
            // (-) / 1
            createLoan("Buana Ahmad", "Karpet", 1, "10/26/2024", "11/02/2024", PeminjamanStatus.TAKEN,
                    formatter);

            // 3. Speaker / Barang / Hendra Saputra / 10/10/2024 - 10/15/2024 / Terlambat
            // (Returned 10/17/2024 - Logic is complicated, let's just use COMPLETED but
            // late date? Or TAKEN if visually matches late logic.
            // HTML says Status: Terlambat, Tgl Kembali: 10/17/2024. If Status is Terlambat,
            // usually implies NOT returned yet or returned late.
            // If I set COMPLETED, it shows "Sudah Kembali".
            // To match visual "Terlambat", I'll set status TAKEN and EndDate past. But HTML
            // says Tgl Kembali 10/17/2024.
            // For now, let's set COMPLETED with actual return date 10/17. The badge logic
            // in HTML might need adjustment or I update status to match visual.
            // Wait, PeminjamanStatus enum doesn't have LATE.
            // Let's set to COMPLETED for history sake, assume "Terlambat" was its state
            // before return.
            // OR the report shows "Terlambat" because it was returned late?
            // The HTML has HARDCODED "Terlambat" text.
            // I will set COMPLETED. The date logic in HTML will handle display if I
            // implement it.
            createLoan("Hendra Saputra", "Speaker", 1, "10/10/2024", "10/15/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter); // Returned late

            // 4. Ruang Utama / Ruangan / Ahmad Abdullah / 09/26/2024 - 09/27/2024 / Sudah
            // Kembali / 1
            createLoan("Ahmad Abdullah", "Ruang Utama", 1, "09/26/2024", "09/27/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter);

            // 5. Pelataran Masjid / Ruangan / Ismail Sulaiman / 10/25/2024 - 10/29/2024 /
            // Sedang Dipinjam / 1
            createLoan("Ismail Sulaiman", "Pelataran Masjid", 1, "10/25/2024", "10/29/2024",
                    PeminjamanStatus.TAKEN,
                    formatter);

            // 6. Meja Kayu / Barang / Putra Idris / 09/29/2024 - 10/01/2024 / Sudah Kembali
            // / 3
            createLoan("Putra Idris", "Meja Kayu", 3, "09/29/2024", "10/01/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter);

            // 7. Terpal / Barang / Wira Cahya / 10/20/2024 - 10/24/2024 / Terlambat
            // (Returned 10/26)
            createLoan("Wira Cahya", "Terpal", 2, "10/20/2024", "10/24/2024", PeminjamanStatus.COMPLETED,
                    formatter);

            // 8. Hijab / Barang / Dina Rahma / 10/27/2024 - 11/03/2024 / Sedang Dipinjam /
            // 4
            createLoan("Dina Rahma", "Hijab", 4, "10/27/2024", "11/03/2024", PeminjamanStatus.TAKEN,
                    formatter);

            // 9. Selasar Selatan / Ruangan / Fatimah / 10/02/2024 - 10/02/2024 / Sudah
            // Kembali / 1
            createLoan("Fatimah", "Selasar Selatan", 1, "10/02/2024", "10/02/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter);

            // 10. Sofa / Barang / Ridho Ali / 10/25/2024 - 11/01/2024 / Sedang Dipinjam / 2
            createLoan("Ridho Ali", "Sofa", 2, "10/25/2024", "11/01/2024", PeminjamanStatus.TAKEN,
                    formatter);

            // 11. Akun Zoom MSU / Barang / Nur Halimah / 09/18/2024 - 09/19/2024 / Sudah
            // Kembali / 1
            createLoan("Nur Halimah", "Akun Zoom MSU", 1, "09/18/2024", "09/19/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter);

            // 12. Lantai 2 Timur / Ruangan / Muhammad Zaki / 10/26/2024 - 10/30/2024 /
            // Sedang Dipinjam / 1
            createLoan("Muhammad Zaki", "Lantai 2 Timur", 1, "10/26/2024", "10/30/2024",
                    PeminjamanStatus.TAKEN,
                    formatter);

            // 13. Meja / Barang / Rahman Mansur / 10/26/2022 - 10/29/2022 / Sudah Kembali /
            // 2
            createLoan("Rahman Mansur", "Meja", 2, "10/26/2022", "10/29/2022", PeminjamanStatus.COMPLETED,
                    formatter);

            // 14. Proyektor / Barang / Putri Melati / 10/17/2024 - 10/20/2024 / Terlambat
            // (Returned 10/22) / 1
            createLoan("Putri Melati", "Proyektor", 1, "10/17/2024", "10/20/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter);

            // 15. Ruang Tamu VIP / Ruangan / Salim Yusuf / 10/15/2024 - 10/15/2024 / Sudah
            // Kembali / 1
            createLoan("Salim Yusuf", "Ruang Tamu VIP", 1, "10/15/2024", "10/15/2024",
                    PeminjamanStatus.COMPLETED,
                    formatter);
        }
    }

    private void seedLaporan() {
        if (laporanRepository.count() == 0) {
            List<Peminjaman> loans = peminjamanRepository.findAll();
            for (Peminjaman p : loans) {
                if (p.getStatus() == PeminjamanStatus.COMPLETED || p.getStatus() == PeminjamanStatus.TAKEN) {
                    com.Habb.InventarisMSU.model.Laporan l = new com.Habb.InventarisMSU.model.Laporan();
                    l.setPeminjaman(p);
                    // Mock times based on loan dates
                    l.setPickedUpAt(p.getStartDate().atTime(9, 0)); // 9 AM pickup
                    if (p.getStatus() == PeminjamanStatus.COMPLETED) {
                        l.setReturnedAt(p.getEndDate().atTime(16, 0)); // 4 PM return
                        l.setSubmitted(true);
                        l.setNotes("Dikembalikan dengan baik.");
                    } else {
                        l.setSubmitted(false);
                        l.setNotes("Sedang dipinjam.");
                    }
                    laporanRepository.save(l);
                }
            }
        }
    }

    private void createLoan(String borrower, String itemName, int qty, String start, String end,
            PeminjamanStatus status, DateTimeFormatter fmt) {
        Item item = itemRepository.findByName(itemName);
        if (item != null) {
            Peminjaman p = new Peminjaman();
            p.setBorrowerName(borrower);
            p.setEmail("user@example.com"); // Dummy
            p.setStartDate(LocalDate.parse(start, fmt));
            p.setEndDate(LocalDate.parse(end, fmt));
            p.setStatus(status);
            p.setHandedOver(true);
            p.setReturned(status == PeminjamanStatus.COMPLETED); // returned if completed

            // Create Detail
            PeminjamanDetail detail = new PeminjamanDetail();
            detail.setItem(item);
            detail.setQuantity(qty);
            detail.setPeminjaman(p); // Link back

            List<PeminjamanDetail> details = new ArrayList<>();
            details.add(detail);
            p.setDetails(details);

            peminjamanRepository.save(p);
        }
    }
}
