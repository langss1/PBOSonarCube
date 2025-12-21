package com.Habb.InventarisMSU.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/pengurus")
public class PengurusController {

    private final com.Habb.InventarisMSU.service.PeminjamanService peminjamanService;
    private final com.Habb.InventarisMSU.repository.LaporanRepository laporanRepository;

    public PengurusController(com.Habb.InventarisMSU.service.PeminjamanService peminjamanService,
            com.Habb.InventarisMSU.repository.LaporanRepository laporanRepository) {
        this.peminjamanService = peminjamanService;
        this.laporanRepository = laporanRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(org.springframework.ui.Model model) {
        // Fetch valid borrowings (e.g., APPROVED)
        // For simplicity, let's fetch all APPROVED ones for now, or filter by date if
        // needed.
        // User asked for "Peminjaman Hari Ini" (Loans Today), implying validation
        // against current date.
        // Let's filter in Java for now.
        var all = peminjamanService.getAllPeminjaman();
        var today = java.time.LocalDate.now();
        var approvedToday = all.stream()
                .filter(p -> p.getStatus() == com.Habb.InventarisMSU.model.PeminjamanStatus.APPROVED
                || p.getStatus() == com.Habb.InventarisMSU.model.PeminjamanStatus.TAKEN)
                .filter(p -> !p.getStartDate().isAfter(today) && !p.getEndDate().isBefore(today)) // Active today
                .toList();

        model.addAttribute("activeLoans", approvedToday);
        return "pengurus/dashboard";
    }

    @GetMapping("/fasilitas")
    public String fasilitas(org.springframework.ui.Model model) {
        var all = peminjamanService.getAllPeminjaman();
        // Filter for APPROVED (ready to take) or TAKEN (ready to return)
        var active = all.stream()
                .filter(p -> p.getStatus() == com.Habb.InventarisMSU.model.PeminjamanStatus.APPROVED
                || p.getStatus() == com.Habb.InventarisMSU.model.PeminjamanStatus.TAKEN)
                .toList();
        model.addAttribute("activeLoans", active);
        return "pengurus/pinjamFasilitas";
    }

    @PostMapping("/fasilitas/update-status")
    public String updateStatus(@RequestParam("id") Long id,
            @RequestParam("action") String action,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        var status = com.Habb.InventarisMSU.model.PeminjamanStatus.valueOf(action);
        var peminjaman = peminjamanService.getPeminjamanById(id);

        try {
            if (peminjaman != null) {
                peminjaman.setStatus(status);
                peminjamanService.save(peminjaman);

                // Handle Laporan Realtime Update
                com.Habb.InventarisMSU.model.Laporan laporan = laporanRepository.findByPeminjamanId(id);
                if (laporan == null) {
                    laporan = new com.Habb.InventarisMSU.model.Laporan();
                    laporan.setPeminjaman(peminjaman);
                }

                if (status == com.Habb.InventarisMSU.model.PeminjamanStatus.TAKEN) {
                    laporan.setPickedUpAt(java.time.LocalDateTime.now());
                    laporanRepository.save(laporan);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Fasilitas berhasil diambil. Jangan lupa mintakan kartu identitas sebagai bukti peminjaman");
                } else if (status == com.Habb.InventarisMSU.model.PeminjamanStatus.COMPLETED) {
                    laporan.setReturnedAt(java.time.LocalDateTime.now());
                    laporan.setSubmitted(true); // Auto submit or ready to submit?
                    laporanRepository.save(laporan);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Fasilitas berhasil dikembalikan. Jangan lupa kembalikan kartu identitas sebagai bukti pengembalian");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage() + ". Coba restart aplikasi atau hubungi admin.");
        }

        return "redirect:/pengurus/fasilitas";
    }

    @GetMapping("/riwayat")
    public String riwayat(org.springframework.ui.Model model) {
        var all = peminjamanService.getAllPeminjaman();
        // Filter for COMPLETED, REJECTED, or TAKEN (User request: TAKEN appears in history too)
        var history = all.stream()
                .filter(p -> p.getStatus() == com.Habb.InventarisMSU.model.PeminjamanStatus.COMPLETED
                || p.getStatus() == com.Habb.InventarisMSU.model.PeminjamanStatus.REJECTED
                || p.getStatus() == com.Habb.InventarisMSU.model.PeminjamanStatus.TAKEN)
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId())) // Newest first
                .toList();
        model.addAttribute("historyLoans", history);
        return "pengurus/riwayat";
    }

    @PostMapping("/riwayat/cancel")
    public String cancelStatus(@RequestParam("id") Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        // Revert status to APPROVED so both checkboxes appear unchecked in pinjamFasilitas
        // logic: undo completion/taking, reset to ready
        var peminjaman = peminjamanService.getPeminjamanById(id);
        if (peminjaman != null) {
            peminjaman.setStatus(com.Habb.InventarisMSU.model.PeminjamanStatus.APPROVED);
            peminjamanService.save(peminjaman);

            // Remove or Reset Laporan
            var laporan = laporanRepository.findByPeminjamanId(id);
            if (laporan != null) {
                laporanRepository.delete(laporan); // Simplest to reset state
            }
        }

        redirectAttributes.addFlashAttribute("successMessage",
                "Status peminjaman berhasil dibatalkan. Data dikembalikan ke Dashboard.");
        return "redirect:/pengurus/riwayat";
    }

    @PostMapping("/riwayat/submit")
    public String submitReport(@RequestParam("id") Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        var laporan = laporanRepository.findByPeminjamanId(id);
        if (laporan != null) {
            laporan.setSubmitted(true);
            laporanRepository.save(laporan);
        } else {
            // Fallback: create if missing (though unlikely if flow followed)
            var peminjaman = peminjamanService.getPeminjamanById(id);
            if (peminjaman != null) {
                laporan = new com.Habb.InventarisMSU.model.Laporan();
                laporan.setPeminjaman(peminjaman);
                laporan.setSubmitted(true);
                laporanRepository.save(laporan);
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Laporan peminjaman berhasil dikirim.");
        return "redirect:/pengurus/riwayat";
    }
}
