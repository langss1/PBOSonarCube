package com.Habb.InventarisMSU.repository;

import com.Habb.InventarisMSU.model.Laporan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaporanRepository extends JpaRepository<Laporan, Long> {

    // defined query methods will go here
    Laporan findByPeminjamanId(Long peminjamanId);
}
