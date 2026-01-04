package com.Habb.InventarisMSU.controller;

import com.Habb.InventarisMSU.dto.AvailabilityDTO;
import com.Habb.InventarisMSU.dto.BookingRequestDTO;
import com.Habb.InventarisMSU.dto.PublicBookingDTO;
import com.Habb.InventarisMSU.service.GuestBookingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/peminjaman")
public class PeminjamanController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PeminjamanController.class);

    private final GuestBookingService guestBookingService;

    public PeminjamanController(GuestBookingService guestBookingService) {
        this.guestBookingService = guestBookingService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitBooking(
            @RequestParam("borrowerName") String borrowerName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("nimNip") String nimNip,
            @RequestParam("department") String department,
            @RequestParam("reason") String reason,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam("startDate") String startDateStr,
            @RequestParam("startTime") String startTimeStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("endTime") String endTimeStr,
            @RequestParam("duration") Integer duration,
            @RequestParam("items") String itemsJson,
            @RequestParam("file") MultipartFile file,
            @RequestParam("identityFile") MultipartFile identityFile,
            @RequestParam(value = "session", required = false) String session) {
        try {
            BookingRequestDTO req = new BookingRequestDTO();
            req.setBorrowerName(borrowerName);
            req.setEmail(email);
            req.setPhone(phone);
            req.setNimNip(nimNip);
            req.setDepartment(department);
            req.setReason(reason);
            req.setDescription(description);
            req.setLocation(location);
            req.setStartDate(startDateStr);
            req.setStartTime(startTimeStr);
            req.setEndDate(endDateStr);
            req.setEndTime(endTimeStr);
            req.setDuration(duration);
            req.setItemsJson(itemsJson);
            req.setSession(session);

            guestBookingService.submitBooking(req, file, identityFile);

            return ResponseEntity.ok().body("{\"message\": \"Booking berhasil disimpan\"}");

        } catch (Exception e) {
            logger.error("Error saving booking", e);
            return ResponseEntity.internalServerError().body("Error saving booking: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<PublicBookingDTO>> getBookings(
            @RequestParam(value = "date", required = false) String dateStr) {
        return ResponseEntity.ok(guestBookingService.getPublicBookings(dateStr));
    }

    @GetMapping("/month")
    public ResponseEntity<List<Integer>> getBookedDays(
            @RequestParam("itemName") String itemName,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        return ResponseEntity.ok(guestBookingService.getBookedDays(itemName, year, month));
    }

    @GetMapping("/check")
    public ResponseEntity<List<AvailabilityDTO>> checkAvailability(
            @RequestParam("startDate") String startDateStr,
            @RequestParam("startTime") String startTimeStr,
            @RequestParam("endDate") String endDateStr,
            @RequestParam("endTime") String endTimeStr) {
        return ResponseEntity
                .ok(guestBookingService.checkAvailability(startDateStr, startTimeStr, endDateStr, endTimeStr));
    }
}
