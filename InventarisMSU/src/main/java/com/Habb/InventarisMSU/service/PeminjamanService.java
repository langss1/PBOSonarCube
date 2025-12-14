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

    // @Autowired
    // private EmailService emailService;

    @Autowired
    private com.Habb.InventarisMSU.repository.ItemRepository itemRepository;

    public Peminjaman createPeminjaman(Peminjaman peminjaman) {
        Peminjaman saved = peminjamanRepository.save(peminjaman);
        // Email removed as per request
        // emailService.sendSimpleMessage(...)
        return saved;
    }

    public List<Peminjaman> getAllPeminjaman() {
        return peminjamanRepository.findAll();
    }

    public Peminjaman getPeminjamanById(Long id) {
        return peminjamanRepository.findById(id).orElse(null);
    }

    @org.springframework.transaction.annotation.Transactional
    public void updateStatus(Long id, PeminjamanStatus status) {
        System.out.println("DEBUG: Updating status for ID " + id + " to " + status);
        Peminjaman p = getPeminjamanById(id);
        if (p != null) {
            System.out.println("DEBUG: Found peminjaman. Current status: " + p.getStatus());
            // Logic for stock deduction/restoration based on status transition
            if (status == PeminjamanStatus.APPROVED && p.getStatus() != PeminjamanStatus.APPROVED) {
                // Deduct stock
                for (com.Habb.InventarisMSU.model.PeminjamanDetail detail : p.getDetails()) {
                    com.Habb.InventarisMSU.model.Item item = detail.getItem();
                    // Assuming quantity is available quantity, check if sufficient
                    if (item.getStock() >= detail.getQuantity()) {
                        item.setStock(item.getStock() - detail.getQuantity());
                        itemRepository.save(item);
                    } else {
                        item.setStock(Math.max(0, item.getStock() - detail.getQuantity()));
                        itemRepository.save(item);
                    }
                }
            } else if ((status == PeminjamanStatus.REJECTED || status == PeminjamanStatus.COMPLETED)
                    && (p.getStatus() == PeminjamanStatus.APPROVED || p.getStatus() == PeminjamanStatus.TAKEN)) {
                // Return stock if it was previously approved/taken and now is rejected or
                // completed
                for (com.Habb.InventarisMSU.model.PeminjamanDetail detail : p.getDetails()) {
                    com.Habb.InventarisMSU.model.Item item = detail.getItem();
                    item.setStock(item.getStock() + detail.getQuantity());
                    itemRepository.save(item);
                }
            }

            if (status == PeminjamanStatus.TAKEN) {
                p.setHandedOver(true);
            } else if (status == PeminjamanStatus.COMPLETED) {
                p.setReturned(true);
            }

            p.setStatus(status);
            peminjamanRepository.save(p);
            System.out.println("DEBUG: Status updated successfully.");
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
}
