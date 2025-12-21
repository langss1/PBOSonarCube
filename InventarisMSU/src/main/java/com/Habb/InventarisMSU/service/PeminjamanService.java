package com.Habb.InventarisMSU.service;

import com.Habb.InventarisMSU.model.Peminjaman;
import com.Habb.InventarisMSU.model.PeminjamanStatus;
import com.Habb.InventarisMSU.repository.PeminjamanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeminjamanService {

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.Habb.InventarisMSU.repository.ItemRepository itemRepository;

    public Peminjaman createPeminjaman(Peminjaman peminjaman) {
        Peminjaman saved = peminjamanRepository.save(peminjaman);
        return saved;
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

    @org.springframework.transaction.annotation.Transactional
    public void updateStatus(Long id, PeminjamanStatus status, String reason) {
        System.out.println("DEBUG: Updating status for ID " + id + " to " + status);
        Peminjaman p = getPeminjamanById(id);
        if (p != null) {
            System.out.println("DEBUG: Found peminjaman. Current status: " + p.getStatus());

            if (status == PeminjamanStatus.TAKEN) {
                p.setHandedOver(true);
            } else if (status == PeminjamanStatus.COMPLETED) {
                p.setReturned(true);
            }

            p.setStatus(status);
            peminjamanRepository.save(p);

            // Send Email Notification based on status
            if (status == PeminjamanStatus.APPROVED) {
                sendApprovalEmail(p);
            } else if (status == PeminjamanStatus.REJECTED) {
                sendRejectionEmail(p, reason);
            }

            System.out.println("DEBUG: Status updated successfully.");
        } else {
            System.out.println("DEBUG: Peminjaman ID " + id + " not found.");
        }
    }

    private void sendApprovalEmail(Peminjaman p) {
        String subject = "Peminjaman Disetujui - Masjid Syamsul Ulum";
        String tanggalStr = p.getStartDate().toString();
        String waktuStr = p.getStartTime() + " s/d " + p.getEndTime() + " WIB";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                        .header { margin-bottom: 20px; }
                        .header-text h2 { margin: 0; color: #000; font-size: 18px; font-weight: bold; }
                        .header-text p { margin: 2px 0; font-size: 12px; color: #555; }
                        .divider { border-top: 3px solid #2e7d32; margin: 15px 0; }
                        .content { padding: 0 10px; }
                        .summary-box { background-color: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 5px solid #2e7d32; }
                        .summary-item { margin-bottom: 8px; display: flex; }
                        .label { font-weight: bold; width: 120px; }
                        .value { flex: 1; }
                        .footer { margin-top: 30px; font-size: 11px; color: #888; border-top: 1px solid #eee; padding-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div style="font-weight: bold; font-size: 20px; color: #004d40;">MASJID SYAMSUL ULUM</div>
                        <div style="font-size: 12px;">Jl. Telekomunikasi No.1, Bandung • Jawa Barat, Indonesia</div>
                    </div>

                    <div class="divider"></div>

                    <div class="content">
                        <p><strong>Halo, %s</strong></p>

                        <p>Selamat! Permohonan peminjaman fasilitas Anda telah <strong>DISETUJUI</strong> oleh pengelola.</p>
                        <p>Silakan datang ke sekretariat DKM Masjid Syamsul Ulum untuk proses pengambilan kunci atau koordinasi lebih lanjut.</p>

                        <div class="summary-box">
                            <div class="summary-item">
                                <span class="label">Keperluan</span>
                                <span class="value">: %s</span>
                            </div>
                            <div class="summary-item">
                                <span class="label">Tanggal</span>
                                <span class="value">: %s</span>
                            </div>
                            <div class="summary-item">
                                <span class="label">Waktu</span>
                                <span class="value">: %s</span>
                            </div>
                            <div class="summary-item">
                                <span class="label">Status</span>
                                <span class="value" style="font-weight: bold; color: #2e7d32;">: DISETUJUI (APPROVED)</span>
                            </div>
                        </div>

                        <p>Terima kasih.</p>

                        <br>
                        <p>Salam hangat,<br><strong>Pengelola MSU</strong></p>
                    </div>

                    <div class="footer">
                        &copy; 2025 Masjid Syamsul Ulum.
                    </div>
                </body>
                </html>
                """
                .formatted(p.getBorrowerName(), p.getReason(), tanggalStr, waktuStr);

        emailService.sendHtmlMessage(p.getEmail(), subject, htmlBody);
    }

    private void sendRejectionEmail(Peminjaman p, String reason) {
        String subject = "Peminjaman Ditolak - Masjid Syamsul Ulum";
        String tanggalStr = p.getStartDate().toString();
        String rejectionReason = (reason != null && !reason.isEmpty()) ? reason
                : "Mohon maaf, fasilitas tidak dapat dipinjamkan saat ini.";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                        .header { margin-bottom: 20px; }
                        .divider { border-top: 3px solid #c62828; margin: 15px 0; }
                        .content { padding: 0 10px; }
                        .summary-box { background-color: #ffebee; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 5px solid #c62828; }
                        .summary-item { margin-bottom: 8px; display: flex; }
                        .label { font-weight: bold; width: 120px; }
                        .value { flex: 1; }
                        .footer { margin-top: 30px; font-size: 11px; color: #888; border-top: 1px solid #eee; padding-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div style="font-weight: bold; font-size: 20px; color: #004d40;">MASJID SYAMSUL ULUM</div>
                        <div style="font-size: 12px;">Jl. Telekomunikasi No.1, Bandung • Jawa Barat, Indonesia</div>
                    </div>

                    <div class="divider"></div>

                    <div class="content">
                        <p><strong>Halo, %s</strong></p>

                        <p>Mohon maaf, permohonan peminjaman fasilitas Anda <strong>DITOLAK</strong>.</p>

                        <div class="summary-box">
                            <div class="summary-item">
                                <span class="label">Keperluan</span>
                                <span class="value">: %s</span>
                            </div>
                            <div class="summary-item">
                                <span class="label">Tanggal</span>
                                <span class="value">: %s</span>
                            </div>
                            <div class="summary-item">
                                <span class="label">Status</span>
                                <span class="value" style="font-weight: bold; color: #c62828;">: DITOLAK (REJECTED)</span>
                            </div>
                            <div class="summary-item">
                                <span class="label">Alasan</span>
                                <span class="value">: %s</span>
                            </div>
                        </div>

                        <p>Silakan ajukan permohonan di waktu lain atau hubungi pengelola untuk informasi lebih lanjut.</p>

                        <p>Terima kasih.</p>

                        <br>
                        <p>Salam hangat,<br><strong>Pengelola MSU</strong></p>
                    </div>

                    <div class="footer">
                        &copy; 2025 Masjid Syamsul Ulum.
                    </div>
                </body>
                </html>
                """
                .formatted(p.getBorrowerName(), p.getReason(), tanggalStr, rejectionReason);

        emailService.sendHtmlMessage(p.getEmail(), subject, htmlBody);
    }

    public void updateHandover(Long id, boolean handedOver) {
        Peminjaman p = getPeminjamanById(id);
        if (p != null) {
            p.setHandedOver(handedOver);
            peminjamanRepository.save(p);
        }
    }

    public void updateReturn(Long id, boolean returned) {
        Peminjaman p = getPeminjamanById(id);
        if (p != null) {
            p.setReturned(returned);
            if (returned && p.isHandedOver()) {
                p.setStatus(PeminjamanStatus.COMPLETED);
            }
            peminjamanRepository.save(p);
        }
    }
}
