package com.Habb.InventarisMSU.service;

import com.Habb.InventarisMSU.model.Peminjaman;
import com.Habb.InventarisMSU.model.PeminjamanStatus;
import com.Habb.InventarisMSU.repository.PeminjamanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeminjamanService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PeminjamanService.class);

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private com.Habb.InventarisMSU.service.EmailService emailService;

    @Autowired
    private com.Habb.InventarisMSU.repository.LaporanRepository laporanRepository;

    @Autowired
    private com.Habb.InventarisMSU.repository.ItemRepository itemRepository;

    public Peminjaman createPeminjaman(Peminjaman peminjaman) {
        return peminjamanRepository.save(peminjaman);
    }

    public void save(Peminjaman peminjaman) {
        peminjamanRepository.save(peminjaman);
    }

    public List<Peminjaman> getAllPeminjaman() {
        return peminjamanRepository.findAll();
    }

    public Peminjaman getPeminjamanById(Long id) {
        return peminjamanRepository.findById(id).orElse(null);
    }

    // ... existing code ...

    @org.springframework.transaction.annotation.Transactional
    public void updateStatus(Long id, PeminjamanStatus status) {
        updateStatus(id, status, null);
    }

    @org.springframework.transaction.annotation.Transactional
    public void updateStatus(Long id, PeminjamanStatus status, String reason) {
        logger.info("Updating status for ID {} to {}", id, status);
        Peminjaman p = getPeminjamanById(id);
        if (p != null) {
            logger.debug("Found peminjaman. Current status: {}", p.getStatus());

            if (status == PeminjamanStatus.TAKEN) {
                p.setHandedOver(true);
            } else if (status == PeminjamanStatus.COMPLETED) {
                p.setReturned(true);
            }

            if (reason != null && !reason.trim().isEmpty()) {
                p.setReason(reason);
            }

            p.setStatus(status);
            peminjamanRepository.save(p);
            logger.info("Status updated successfully to {}", status);

            // SEND EMAIL NOTIFICATION
            if (status == PeminjamanStatus.APPROVED) {
                sendApprovalEmail(p);
            } else if (status == PeminjamanStatus.REJECTED) {
                sendRejectionEmail(p, reason);
            }
        } else {
            logger.warn("Peminjaman ID {} not found during updateStatus.", id);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public String updateStatusAndLaporan(Long id, PeminjamanStatus status) {
        Peminjaman peminjaman = getPeminjamanById(id);
        if (peminjaman == null) {
            return "Peminjaman tidak ditemukan";
        }

        peminjaman.setStatus(status);
        save(peminjaman);

        // Handle Laporan Realtime Update
        com.Habb.InventarisMSU.model.Laporan laporan = laporanRepository.findByPeminjamanId(id);
        if (laporan == null) {
            laporan = new com.Habb.InventarisMSU.model.Laporan();
            laporan.setPeminjaman(peminjaman);
        }

        String message = "";
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (status == PeminjamanStatus.TAKEN) {
            laporan.setPickedUpAt(now);
            message = "Fasilitas berhasil diambil. Jangan lupa mintakan kartu identitas sebagai bukti peminjaman";
        } else if (status == PeminjamanStatus.RETURNED) {
            laporan.setReturnedAt(now);
            if (laporan.getPickedUpAt() == null) {
                laporan.setPickedUpAt(now);
            }

            java.time.LocalDateTime deadline = java.time.LocalDateTime.of(peminjaman.getEndDate(),
                    peminjaman.getEndTime() != null ? peminjaman.getEndTime() : java.time.LocalTime.MAX);

            if (now.isAfter(deadline)) {
                peminjaman.setStatus(PeminjamanStatus.OVERDUE);
                message = "Fasilitas berhasil dikembalikan (Terlambat). Jangan lupa kembalikan kartu identitas.";
            } else {
                message = "Fasilitas berhasil dikembalikan. Jangan lupa kembalikan kartu identitas.";
            }
            save(peminjaman); // re-save for OVERDUE status if changed
        }

        laporanRepository.save(laporan);
        return message;
    }

    private void sendApprovalEmail(Peminjaman p) {
        String subject = "Peminjaman Disetujui - Masjid Syamsul Ulum";
        String htmlBody = com.Habb.InventarisMSU.util.EmailHelper.buildStatusUpdateEmail(p, "Disetujui",
                "Selamat, pengajuan peminjaman Anda telah <strong>DISETUJUI</strong> oleh pengelola.",
                null);
        emailService.sendHtmlMessage(p.getEmail(), subject, htmlBody);
    }

    private void sendRejectionEmail(Peminjaman p, String reason) {
        String subject = "Peminjaman Ditolak - Masjid Syamsul Ulum";
        String htmlBody = com.Habb.InventarisMSU.util.EmailHelper.buildStatusUpdateEmail(p, "Ditolak",
                "Mohon maaf, pengajuan peminjaman Anda <strong>DITOLAK</strong> oleh pengelola.",
                reason);
        emailService.sendHtmlMessage(p.getEmail(), subject, htmlBody);
    }

}
