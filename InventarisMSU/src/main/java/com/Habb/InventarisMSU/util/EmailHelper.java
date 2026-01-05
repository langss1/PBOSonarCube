package com.Habb.InventarisMSU.util;

import com.Habb.InventarisMSU.model.Peminjaman;

public class EmailHelper {

    private EmailHelper() {
        // Private constructor to hide the implicit public one
    }

    public static String buildBookingConfirmationEmail(Peminjaman p) {
        String tanggalStr = p.getStartDate().toString();
        // Use a simpler concatenation or a separate formatter if needed, keeping it
        // simple for now
        String waktuStr = p.getStartTime() + " s/d " + p.getEndTime() + " WIB";

        return String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <style>\n" +
                        "        body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }\n" +
                        "        .header { margin-bottom: 20px; }\n" +
                        "        .header img { max-height: 60px; float: left; margin-right: 15px; }\n" +
                        "        .header-text { overflow: hidden; }\n" +
                        "        .header-text h2 { margin: 0; color: #000; font-size: 18px; font-weight: bold; }\n" +
                        "        .header-text p { margin: 2px 0; font-size: 12px; color: #555; }\n" +
                        "        .divider { border-top: 3px solid #d32f2f; margin: 15px 0; }\n" +
                        "        .content { padding: 0 10px; }\n" +
                        "        .summary-box { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }\n"
                        +
                        "        .summary-item { margin-bottom: 8px; display: flex; }\n" +
                        "        .label { font-weight: bold; width: 120px; }\n" +
                        "        .value { flex: 1; }\n" +
                        "        .footer { margin-top: 30px; font-size: 11px; color: #888; border-top: 1px solid #eee; padding-top: 10px; }\n"
                        +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"header\">\n" +
                        "        <!-- Placeholder for Logo if needed, or just text -->\n" +
                        "        <div style=\"font-weight: bold; font-size: 20px; color: #004d40;\">MASJID SYAMSUL ULUM</div>\n"
                        +
                        "        <div style=\"font-size: 12px;\">Jl. Telekomunikasi No.1, Bandung • Jawa Barat, Indonesia</div>\n"
                        +
                        "        <div style=\"font-size: 12px;\">Telp: +62 882-7982-9071 • Email: msu.telyu@gmail.com</div>\n"
                        +
                        "    </div>\n" +
                        "\n" +
                        "    <div class=\"divider\"></div>\n" +
                        "\n" +
                        "    <div class=\"content\">\n" +
                        "        <p><strong>Halo, %s</strong></p>\n" +
                        "\n" +
                        "        <p>Terima kasih telah melakukan peminjaman fasilitas di Masjid Syamsul Ulum. Permohonan Anda telah kami terima dan saat ini sedang dalam proses peninjauan oleh pengelola.</p>\n"
                        +
                        "\n" +
                        "        <p>Berikut adalah ringkasan permohonan Anda:</p>\n" +
                        "\n" +
                        "        <div class=\"summary-box\">\n" +
                        "            <div class=\"summary-item\">\n" +
                        "                <span class=\"label\">Keperluan</span>\n" +
                        "                <span class=\"value\">: %s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"summary-item\">\n" +
                        "                <span class=\"label\">Tanggal</span>\n" +
                        "                <span class=\"value\">: %s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"summary-item\">\n" +
                        "                <span class=\"label\">Waktu</span>\n" +
                        "                <span class=\"value\">: %s</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"summary-item\">\n" +
                        "                <span class=\"label\">Status</span>\n" +
                        "                <span class=\"value\" style=\"font-weight: bold;\">: Menunggu Persetujuan</span>\n"
                        +
                        "            </div>\n" +
                        "        </div>\n" +
                        "\n" +
                        "        <p>Kami akan memberitahukan status selanjutnya melalui email ini setelah permohonan Anda ditinjau.</p>\n"
                        +
                        "\n" +
                        "        <p>Terima kasih atas perhatian dan kerjasamanya.</p>\n" +
                        "\n" +
                        "        <br>\n" +
                        "        <p>Salam hangat,<br><strong>Pengelola MSU</strong></p>\n" +
                        "    </div>\n" +
                        "\n" +
                        "    <div class=\"footer\">\n" +
                        "        &copy; 2025 Masjid Syamsul Ulum Telkom University. All rights reserved.<br>\n" +
                        "        Email ini dibuat secara otomatis, mohon tidak membalas email ini.\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>",
                p.getBorrowerName(), p.getReason(), tanggalStr, waktuStr);
    }
}
