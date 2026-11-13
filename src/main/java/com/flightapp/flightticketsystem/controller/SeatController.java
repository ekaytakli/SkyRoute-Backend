package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.dto.SeatRequestDto;
import com.flightapp.flightticketsystem.dto.SeatResponseDto;
import com.flightapp.flightticketsystem.service.ISeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Uçak koltuklarının listelenmesi, müsaitlik kontrolü, eklenmesi, güncellenmesi
 * ve silinmesi gibi HTTP isteklerini karşılayan REST Controller bileşenidir[cite: 1].
 */
@RestController // REST mimarisinde hizmet verdiğini ve yanıtların doğrudan JSON formatında döneceğini belirtir[cite: 1].
@RequestMapping("/api/v1/seats") // Bu sınıftaki tüm uç noktaların temel URL ön ekini (/api/v1/seats) belirler[cite: 1].
@RequiredArgsConstructor // 'final' olarak işaretlenen ISeatService bağımlılığı için otomatik Constructor Injection oluşturur[cite: 1].
public class SeatController {

    // Koltuk CRUD işlemlerini ve iş kurallarını yöneten servis arayüzü[cite: 1].
    private final ISeatService seatService;

    /**
     * Belirli bir uçuşa yeni bir koltuk tanımlayan HTTP POST uç noktasıdır[cite: 1].
     * Rota: POST /api/v1/seats/add/seats[cite: 1]
     *
     * @param requestDto Tanımlanacak koltuğun numarasını, tipini, fiyatını ve flightId bilgisini taşıyan DTO[cite: 1].
     *                   @Valid: Alanların (@NotBlank, @NotNull) kurallara uygunluğunu denetler[cite: 1].
     *                   @RequestBody: İstek gövdesindeki (body) JSON verisini SeatRequestDto nesnesine çevirir[cite: 1].
     * @return Sisteme kaydedilen yeni koltuğun detaylarını içeren SeatResponseDto nesnesi[cite: 1].
     */
    @PostMapping("/add/seats")
    @PreAuthorize("hasRole('ADMIN')")
    public SeatResponseDto addSeat(@Valid @RequestBody SeatRequestDto requestDto) {
        // Koltuk ekleme işlemini doğrudan servis katmanına devreder[cite: 1].
        return seatService.addSeat(requestDto);
    }

    /**
     * ID'si verilen bir uçuşa ait olan tüm koltukları (dolu ve boş fark etmeksizin) getiren HTTP GET uç noktasıdır[cite: 1].
     * Rota: GET /api/v1/seats/flight/{flightId}[cite: 1]
     *
     * @param flightId URL yolu üzerinden gönderilen uçuşun benzersiz kimlik numarası[cite: 1].
     * @return Uçuşa ait koltukların listesi[cite: 1].
     */
    @GetMapping("/flight/{flightId}")
    public List<SeatResponseDto> getSeatsByFlightId(@PathVariable Integer flightId) {
        // Belirtilen uçuşun koltuk haritasını getirmesi için servisi çağırır[cite: 1].
        return seatService.getSeatsByFlightId(flightId);
    }

    /**
     * Belirli bir uçuşta henüz satılmamış, rezervasyona açık (müsait) koltukları listeleyen HTTP GET uç noktasıdır[cite: 1].
     * Rota: GET /api/v1/seats/available/{flightId}[cite: 1]
     *
     * Yolcuların bilet satın alma ekranında sadece boş koltukları görebilmesi için kullanılır[cite: 1].
     *
     * @param flightId Koltukları filtrelenecek uçuşun kimlik numarası[cite: 1].
     * @return Sadece satın alınabilir durumdaki koltukların listesi[cite: 1].
     */
    @GetMapping("/available/{flightId}")
    public List<SeatResponseDto> getAvailableSeatsByFlightId(@PathVariable Integer flightId) {
        // Servis katmanından sadece isAvailable = true olan koltukları talep eder[cite: 1].
        return seatService.getAvailableSeatsByFlightId(flightId);
    }

    /**
     * Mevcut bir koltuğun bilgilerini (fiyat, sınıf, numara veya uçuş bağlantısı) güncelleyen HTTP PUT uç noktasıdır[cite: 1].
     * Rota: PUT /api/v1/seats/update/seats/{id}[cite: 1]
     *
     * @param id Güncellenmek istenen koltuğun benzersiz kimlik numarası[cite: 1].
     * @param requestDto Güncellenecek yeni koltuk verilerini taşıyan DTO[cite: 1].
     * @return Güncelleme sonrası oluşan en güncel koltuk bilgisi[cite: 1].
     */
    @PutMapping("/update/seats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SeatResponseDto updateSeat(@PathVariable Integer id, @Valid @RequestBody SeatRequestDto requestDto) {
        // Değişiklikleri kaydetmesi için ID ve yeni verileri servis katmanına aktarır[cite: 1].
        return seatService.updateSeat(id, requestDto);
    }

    /**
     * Belirtilen ID'ye sahip koltuğu sistemden ve veritabanından kalıcı olarak silen HTTP DELETE uç noktasıdır[cite: 1].
     * Rota: DELETE /api/v1/seats/{id}[cite: 1]
     *
     * @PreAuthorize: Bu işlemi yalnızca sistemde 'ADMIN' rolü bulunan kullanıcılar gerçekleştirebilir[cite: 1].
     * @param id Silinmesi istenen koltuğun kimlik numarası[cite: 1].
     */
    @PreAuthorize("hasRole('ADMIN')") // Yetkisiz kullanıcıların koltuk silmesini engeller[cite: 1].
    @DeleteMapping("/{id}")
    public void deleteSeat(@PathVariable Integer id) {
        // Silme komutunu servis katmanına iletir[cite: 1].
        seatService.deleteSeat(id);
    }
}
