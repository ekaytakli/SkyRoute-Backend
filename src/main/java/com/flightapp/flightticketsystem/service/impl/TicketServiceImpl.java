package com.flightapp.flightticketsystem.service.impl;

import com.flightapp.flightticketsystem.dto.TicketRequestDto;
import com.flightapp.flightticketsystem.dto.TicketResponseDto;
import com.flightapp.flightticketsystem.entities.Flight;
import com.flightapp.flightticketsystem.entities.Seat;
import com.flightapp.flightticketsystem.entities.Ticket;
import com.flightapp.flightticketsystem.entities.User;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.repository.FlightRepository;
import com.flightapp.flightticketsystem.repository.SeatRepository;
import com.flightapp.flightticketsystem.repository.TicketRepository;
import com.flightapp.flightticketsystem.repository.UserRepository;
import com.flightapp.flightticketsystem.service.ITicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Bilet oluşturma ve kullanıcının geçmiş biletlerini sorgulama iş mantığını yürüten servis sınıfıdır[cite: 1].
 */
@Service // Spring konteynerine bu sınıfın bir Service bileşeni olduğunu bildirir.
@RequiredArgsConstructor // 'final' olarak işaretlenmiş tüm bağımlılıklar için constructor üretir (Constructor Injection).
public class TicketServiceImpl implements ITicketService {

    // Bilet veritabanı kayıt ve sorgulama işlemlerini yürüten repository.
    private final TicketRepository ticketRepository;

    // Biletin ait olduğu uçuşun varlığını kontrol etmek için kullanılan repository.
    private final FlightRepository flightRepository;

    // Koltuğun doluluk durumu ve ait olduğu uçuşu doğrulamak/güncellemek için kullanılan repository.
    private final SeatRepository seatRepository;

    // Oturum açmış kullanıcının bilgilerini veritabanından çekmek için kullanılan repository.
    private final UserRepository userRepository;

    /**
     * Yeni bir bilet oluşturur, seçilen koltuğu rezerve eder (dolu yapar) ve gerekiyorsa kullanıcıya bağlar[cite: 1].
     *
     * @param requestDto Seçilen uçuş ID'si ve koltuk ID'sini içeren DTO.
     * @return Oluşturulan biletin PNR kodu ve detaylarını içeren Response DTO.
     * @throws BaseException Uçuş veya koltuk bulunamazsa, koltuk o uçuşa ait değilse ya da koltuk doluysa fırlatılır.
     */
    @Override
    public TicketResponseDto createTicket(TicketRequestDto requestDto) {

        // 1. Adım: Verilen flightId ile uçuşun sistemde mevcut olup olmadığını doğrula.
        Flight flight = flightRepository
                .findById(requestDto.getFlightId())
                .orElseThrow(() -> new BaseException("error.flight_not_found"));

        // 2. Adım: Verilen seatId ile koltuğun sistemde kayıtlı olup olmadığını doğrula.
        Seat seat = seatRepository
                .findById(requestDto.getSeatId())
                .orElseThrow(() -> new BaseException("error.seat_not_found"));

        // 3. Adım: Güvenlik Kontrolü - Seçilen koltuk gerçekten seçilen uçuşa mı ait?
        if (!seat.getFlight().getId().equals(flight.getId())) {
            throw new BaseException("error.seat_not_belong_to_flight");
        }

        // 4. Adım: Koltuğun müsaitlik durumunu kontrol et; koltuk önceden satılmışsa işlemi durdur.
        if (!seat.isAvailable()) {
            throw new BaseException("error.seat_not_available");
        }

        // 5. Adım: Yeni Ticket entity nesnesi oluştur ve temel alanlarını doldur.
        Ticket ticket = new Ticket();

        // 6 haneli, büyük harflerden oluşan tekil bir PNR rezervasyon kodu üret.
        ticket.setPnrKodu(
                UUID.randomUUID()
                        .toString()
                        .substring(0, 6)
                        .toUpperCase()
        );

        // Biletin kesilme tarihini ve ilişkili olduğu uçuş/koltuk bilgilerini ata.
        ticket.setDate(LocalDateTime.now());
        ticket.setFlight(flight);
        ticket.setSeat(seat);

        // 6. Adım: Güvenlik bağlamından (Security Context) oturum açan kullanıcıyı kontrol et.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Eğer kullanıcı anonim değilse ve sisteme login olmuşsa, bileti bu kullanıcıya bağla.
        if (
                authentication != null &&
                        authentication.isAuthenticated() &&
                        !"anonymousUser".equals(authentication.getPrincipal())
        ) {
            String email = authentication.getName();

            // Oturum açan kullanıcının güncel kaydını veritabanından getir.
            User user = userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new BaseException("error.user_not_found"));

            // Bileti kullanıcı entity'si ile ilişkilendir.
            ticket.setUser(user);
        }
        // (Kullanıcı giriş yapmadan bilet alıyorsa ticket.user alanı null kalır)

        // 7. Adım: Oluşturulan bileti veritabanına kaydet.
        Ticket savedTicket = ticketRepository.save(ticket);

        // 8. Adım: Koltuğu müsait değil (dolu) olarak işaretle ve veritabanında güncelle.
        seat.setAvailable(false);
        seatRepository.save(seat);

        // 9. Adım: Kaydedilen bilet entity'sini DTO nesnesine çevirip döndür.
        return mapToResponse(savedTicket);
    }

    /**
     * Sisteme giriş yapmış olan kullanıcının satın aldığı tüm biletleri listeler.
     *
     * @return Giriş yapan kullanıcıya ait biletlerin DTO listesi.
     * @throws BaseException Kullanıcı giriş yapmamışsa (unauthorized) veya kullanıcı bulunamazsa fırlatılır.
     */
    @Override
    public List<TicketResponseDto> getMyTickets() {

        // 1. Adım: Güvenlik bağlamından aktif oturumu al.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Adım: Kullanıcı oturum açmamışsa veya anonim ise erişimi engelle ve yetkisiz işlem hatası fırlat.
        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(authentication.getPrincipal())
        ) {
            throw new BaseException("error.unauthorized");
        }

        // 3. Adım: Oturumdaki kullanıcının e-posta adresini alıp veritabanındaki kaydına ulaş.
        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new BaseException("error.user_not_found"));

        // 4. Adım: Bu kullanıcı ID'sine ait tüm biletleri veritabanından çek, her birini DTO'ya dönüştürüp liste olarak dön.
        return ticketRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Bilet Entity modelini istemciye dönecek olan TicketResponseDto modeline dönüştüren yardımcı metot.
     *
     * @param ticket Veritabanından gelen Bilet entity nesnesi.
     * @return Frontend'e gönderilecek yanıt DTO'su.
     */
    private TicketResponseDto mapToResponse(Ticket ticket) {
        return TicketResponseDto.builder()
                .id(ticket.getId())
                .pnrKodu(ticket.getPnrKodu())
                .date(ticket.getDate())
                .flightId(ticket.getFlight().getId())
                .seatId(ticket.getSeat().getId())
                .seatNumber(ticket.getSeat().getSeatNumber())
                .build();
    }
}