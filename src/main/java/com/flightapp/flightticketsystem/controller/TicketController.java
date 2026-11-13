package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.dto.TicketRequestDto;
import com.flightapp.flightticketsystem.dto.TicketResponseDto;
import com.flightapp.flightticketsystem.service.ITicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Bilet oluşturma ve kullanıcının sahip olduğu biletleri listeleme
 * işlemlerini yöneten REST Controller bileşenidir[cite: 1].
 */
@RestController // Bu sınıfın REST API uç noktaları sağladığını ve dönen verilerin doğrudan JSON formatında sunulacağını belirtir[cite: 1].
@RequestMapping("/api/v1/tickets") // Sınıf içindeki tüm bilet uç noktalarının kök URL ön ekini (/api/v1/tickets) belirler[cite: 1].
@RequiredArgsConstructor // 'final' tanımlı ITicketService bağımlılığı için Constructor Injection sağlar[cite: 1].
public class TicketController {

    // Bilet oluşturma, doğrulama ve kullanıcı biletlerini çekme iş mantığını yürüten servis arayüzü[cite: 1].
    private final ITicketService ticketService;

    /**
     * Seçilen uçuş ve koltuk ID'si doğrultusunda yeni bir bilet oluşturan HTTP POST uç noktasıdır[cite: 1].
     * Rota: POST /api/v1/tickets[cite: 1]
     *
     * Doğrudan (ödemesiz) bilet oluşturma yalnızca yöneticilere açıktır.
     * Müşteriler ve misafirler bilet almak için ödeme uç noktasını kullanır.
     *
     * @param requestDto Biletin ait olacağı flightId ve seatId bilgilerini taşıyan DTO[cite: 1].
     *                   @Valid: Alanların (@NotNull) doğruluğunu denetler[cite: 1].
     *                   @RequestBody: İstek gövdesindeki (body) JSON verisini Java TicketRequestDto nesnesine çevirir[cite: 1].
     * @return Üretilen biletin PNR kodunu, kesim tarihini ve detaylarını içeren TicketResponseDto nesnesi[cite: 1].
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TicketResponseDto createTicket(
            @Valid @RequestBody TicketRequestDto requestDto
    ) {
        // Bilet kesim sürecini başlatması için isteği doğrudan ITicketService katmanına devreder[cite: 1].
        return ticketService.createTicket(requestDto);
    }

    /**
     * Sisteme giriş yapmış olan kullanıcının kendi hesabına kayıtlı biletlerini getiren HTTP GET uç noktasıdır[cite: 1].
     * Rota: GET /api/v1/tickets/my-tickets[cite: 1]
     *
     * @PreAuthorize: Bu uç noktaya sadece 'CUSTOMER' veya 'ADMIN' rolüne sahip,
     * geçerli bir JWT token ile kimliği doğrulanmış kullanıcılar erişebilir[cite: 1].
     *
     * @return Giriş yapan kullanıcının satın aldığı tüm biletlerin TicketResponseDto listesi[cite: 1].
     */
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @GetMapping("/my-tickets")
    public List<TicketResponseDto> getMyTickets() {
        // Servis katmanından oturum açmış kullanıcının bilet listesini çeker ve döner[cite: 1].
        return ticketService.getMyTickets();
    }
}
