package com.flightapp.flightticketsystem.service.impl;

import com.flightapp.flightticketsystem.dto.SeatRequestDto;
import com.flightapp.flightticketsystem.dto.SeatResponseDto;
import com.flightapp.flightticketsystem.entities.Flight;
import com.flightapp.flightticketsystem.entities.Seat;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.mapper.SeatMapper;
import com.flightapp.flightticketsystem.repository.FlightRepository;
import com.flightapp.flightticketsystem.repository.SeatRepository;
import com.flightapp.flightticketsystem.service.ISeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Uçuşlara ait koltukların yönetimini (ekleme, listeleme, güncelleme, silme ve doluluk sorgulama)
 * sağlayan servis implementasyon sınıfıdır[cite: 1].
 */
@Service // Spring konteynerine bu sınıfın bir Service katmanı bileşeni olduğunu bildirir.
@RequiredArgsConstructor // 'final' tanımlı alanlar için otomatik Constructor oluşturur (Dependency Injection).
public class SeatServiceImpl implements ISeatService {

    // Koltuk verilerini veritabanında sorgulamak, kaydetmek ve güncellemek için kullanılan repository.
    private final SeatRepository seatRepository;

    // Seat Entity ile Seat DTO sınıfları arasındaki dönüşümleri sağlayan mapper.
    private final SeatMapper seatMapper;

    // Koltuğun bağlanacağı uçuşun varlığını kontrol etmek için kullanılan uçuş repository'si.
    private final FlightRepository flightRepository;

    /**
     * Belirtilen bir uçuşa yeni bir koltuk ekler.
     *
     * @param requestDto Eklenecek koltuğun numarasını, fiyatını ve bağlı olduğu flightId'yi içeren DTO.
     * @return Kaydedilen yeni koltuğun yanıt modeli (SeatResponseDto).
     * @throws BaseException Koltuğun ekleneceği uçuş veritabanında bulunamazsa fırlatılır.
     */
    @Override
    public SeatResponseDto addSeat(SeatRequestDto requestDto) {
        // 1. Adım: Koltuğun tanımlanacağı uçuşun veritabanında gerçekten var olup olmadığını kontrol et.
        Flight flight = flightRepository.findById(requestDto.getFlightId())
                .orElseThrow(() -> new BaseException("error.flight_not_found"));

        // 2. Adım: Gelen DTO verisini Seat veritabanı varlığına (Entity) dönüştür.
        Seat seat = seatMapper.toEntity(requestDto);

        // 3. Adım: Koltuğu doğrulanan ilgili uçuş nesnesiyle ilişkilendir (Foreign Key bağlamı).
        seat.setFlight(flight);

        // 4. Adım: İlişkilendirilen koltuğu veritabanına kaydet.
        Seat savedSeat = seatRepository.save(seat);

        // 5. Adım: Kaydedilen entity'yi kullanıcıya gösterilecek response DTO nesnesine çevirip döndür.
        return seatMapper.toDto(savedSeat);
    }

    /**
     * Belirtilen bir uçuşa ait olan tüm koltukları (dolu ve boş fark etmeksizin) getirir.
     *
     * @param flightId Koltukları listelenecek uçuşun ID değeri.
     * @return Uçuşa kayıtlı tüm koltukların DTO listesi.
     * @throws BaseException Uçuş bulunamazsa fırlatılır.
     */
    @Override
    public List<SeatResponseDto> getSeatsByFlightId(Integer flightId) {

        // 1. Adım: Uçuşun geçerliliğini doğrula (olmayan bir uçuş için koltuk aramamak adına).
        flightRepository.findById(flightId)
                .orElseThrow(() -> new BaseException("error.flight_not_found"));

        // 2. Adım: O uçuş ID'sine bağlı olan tüm koltuk kayıtlarını veritabanından çek.
        List<Seat> seats = seatRepository.findByFlightId(flightId);

        // 3. Adım: Entity listesini DTO listesine dönüştürerek döndür.
        return seatMapper.toDtoList(seats);
    }

    /**
     * Belirtilen bir uçuşta henüz satın alınmamış (müsait / isAvailable = true) koltukları getirir.
     *
     * @param flightId Müsait koltukları aranacak uçuşun ID değeri.
     * @return Sadece boş ve satın alınabilir durumdaki koltukların DTO listesi.
     * @throws BaseException Uçuş bulunamazsa fırlatılır.
     */
    @Override
    public List<SeatResponseDto> getAvailableSeatsByFlightId(Integer flightId) {
        // 1. Adım: Uçuşun sistemde mevcut olup olmadığını doğrula.
        flightRepository.findById(flightId)
                .orElseThrow(() -> new BaseException("error.flight_not_found"));

        // 2. Adım: Belirtilen uçuşa ait ve isAvailable alanı true olan boş koltukları sorgula.
        List<Seat> availableSeats = seatRepository.findByFlightIdAndIsAvailable(flightId, true);

        // 3. Adım: Filtrelenmiş koltukları DTO listesine çevirip döndür.
        return seatMapper.toDtoList(availableSeats);
    }

    /**
     * Mevcut bir koltuğun bilgilerini (numara, fiyat, durum veya uçuş değişikliği) günceller.
     *
     * @param id Güncellenecek koltuğun birincil anahtarı (ID).
     * @param requestDto Güncel koltuk verilerini taşıyan DTO nesnesi.
     * @return Güncellenmiş koltuğun yanıt modeli.
     * @throws BaseException Koltuk veya atanmak istenen yeni uçuş bulunamazsa fırlatılır.
     */
    @Override
    @Transactional
    public SeatResponseDto updateSeat(Integer id, SeatRequestDto requestDto) {
        // 1. Adım: Güncellenmek istenen koltuğu ID ile bul, bulunamazsa hata fırlat.
        Seat seat = seatRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BaseException("error.seat_not_found"));

        if (seat.getTicket() != null) {
            throw new BaseException("error.seat_has_ticket");
        }

        // 2. Adım: Koltuğun bağlı olduğu uçuş değiştirilmek istenmiş mi kontrol et.
        if (!seat.getFlight().getId().equals(requestDto.getFlightId())) {
            // Değiştirilmek isteniyorsa, yeni uçuşun veritabanında var olduğunu doğrula ve koltuğa ata.
            Flight newFlight = flightRepository.findById(requestDto.getFlightId())
                    .orElseThrow(() -> new BaseException("error.flight_not_found"));
            seat.setFlight(newFlight);
        }

        // 3. Adım: Koltuk numarası, fiyat vb. diğer alanları DTO'dan alıp mevcut entity üzerine aktar.
        seatMapper.updateEntityFromDto(requestDto, seat);

        // 4. Adım: Güncellenmiş halini veritabanına yeniden kaydet.
        Seat updatedSeat = seatRepository.save(seat);

        // 5. Adım: Güncel veriyi Response DTO olarak geri döndür.
        return seatMapper.toDto(updatedSeat);
    }

    /**
     * Belirtilen ID'ye sahip koltuğu sistemden ve veritabanından kalıcı olarak siler.
     *
     * @param id Silinmesi istenen koltuğun ID değeri.
     * @throws BaseException Silinecek koltuk sistemde kayıtlı değilse fırlatılır.
     */
    @Override
    @Transactional
    public void deleteSeat(Integer id) {
        // 1. Adım: Silinecek koltuğun veritabanında olup olmadığını kontrol et.
        Seat seat = seatRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BaseException("error.seat_not_found"));

        // 2. Adım: Koltuk kaydını veritabanından sil.
        seatRepository.delete(seat);
    }
}
