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
    private com.Habb.InventarisMSU.service.EmailService emailService;
    
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
    public void updateStatus(Long id, PeminjamanStatus status) {
        updateStatus(id, status, null);
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

            if (reason != null && !reason.trim().isEmpty()) {
                p.setReason(reason);
            }

            p.setStatus(status);
            peminjamanRepository.save(p);
            System.out.println("DEBUG: Status updated successfully.");

            // SEND EMAIL NOTIFICATION
            if (status == PeminjamanStatus.APPROVED) {
                sendApprovalEmail(p);
            } else if (status == PeminjamanStatus.REJECTED) {
                sendRejectionEmail(p, reason);
            }
        } else {
            System.out.println("DEBUG: Peminjaman ID " + id + " not found.");
        }
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

    private void sendApprovalEmail(Peminjaman p) {
        String subject = "Peminjaman Disetujui - Masjid Syamsul Ulum";
        String htmlBody = buildEmailBody(p, "Disetujui", 
            "Selamat, pengajuan peminjaman Anda telah <strong>DISETUJUI</strong> oleh pengelola.",
            null);
        emailService.sendHtmlMessage(p.getEmail(), subject, htmlBody);
    }

    private void sendRejectionEmail(Peminjaman p, String reason) {
        String subject = "Peminjaman Ditolak - Masjid Syamsul Ulum";
        String htmlBody = buildEmailBody(p, "Ditolak", 
            "Mohon maaf, pengajuan peminjaman Anda <strong>DITOLAK</strong> oleh pengelola.",
            reason);
        emailService.sendHtmlMessage(p.getEmail(), subject, htmlBody);
    }

    private String buildEmailBody(Peminjaman p, String statusLabel, String message, String reason) {
        String tanggalStr = p.getStartDate().toString();
        String waktuStr = (p.getStartTime() != null ? p.getStartTime() : "00:00") + " s/d " + (p.getEndTime() != null ? p.getEndTime() : "00:00") + " WIB";
        
        // Items list
        StringBuilder itemsHtml = new StringBuilder();
        if(p.getDetails() != null) {
            for(var d : p.getDetails()) {
                itemsHtml.append(d.getItem().getName()).append(" (").append(d.getQuantity()).append("), ");
            }
        }
        String itemsStr = itemsHtml.length() > 0 ? itemsHtml.substring(0, itemsHtml.length() - 2) : "-";

        String reasonHtml = "";
        if (reason != null && !reason.isEmpty()) {
            reasonHtml = String.format("""
                            <div class="summary-item" style="color: red;">
                                <span class="label">Alasan</span>
                                <span class="value">: %s</span>
                            </div>
                         """, reason);
        }

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                        .header { margin-bottom: 20px; }
                        .header-text h2 { margin: 0; color: #000; font-size: 18px; font-weight: bold; }
                        .divider { border-top: 3px solid #d32f2f; margin: 15px 0; }
                        .content { padding: 0 10px; }
                        .summary-box { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
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
                        <div style="font-size: 12px;">Telp: +62 882-7982-9071 • Email: msu.telyu@gmail.com</div>
                    </div>
                    <div class="divider"></div>
                    <div class="content">
                        <p><strong>Halo, %s</strong></p>
                        <p>%s</p>
                        <div class="summary-box">
                            <div class="summary-item">
                                <span class="label">Barang/Ruang</span>
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
                                <span class="value" style="font-weight: bold;">: %s</span>
                            </div>
                            %s
                        </div>
                        <p>Silakan hubungi kami jika ada pertanyaan.</p>
                        <br>
                        <p>Salam hangat,<br><strong>Pengelola MSU</strong></p>
                    </div>
                    <div class="footer">
                        &copy; 2025 Masjid Syamsul Ulum Telkom University.<br>
                        Email ini dibuat secara otomatis.
                    </div>
                </body>
                </html>
                """.formatted(p.getBorrowerName(), message, itemsStr, tanggalStr, waktuStr, statusLabel, reasonHtml);
    }
}
