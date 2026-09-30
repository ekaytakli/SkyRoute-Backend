package com.flightapp.flightticketsystem.service.impl;

import com.flightapp.flightticketsystem.dto.FlightRequest;
import com.flightapp.flightticketsystem.dto.FlightResponse;
import com.flightapp.flightticketsystem.entities.Flight;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.mapper.FlightMapper;
import com.flightapp.flightticketsystem.repository.FlightRepository;
import com.flightapp.flightticketsystem.service.IFlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Spring IoC konteynerine bu sınıfın bir Service katmanı bileşeni olduğunu ve yönetilmesi gerektiğini belirtir.
@Service
// Lombok: 'final' anahtar kelimesiyle tanımlanmış repository ve mapper bağımlılıkları için otomatik yapıcı metot (constructor injection) oluşturur.
@RequiredArgsConstructor
public class FlightService implements IFlightService {

    // Uçuş verilerinin veritabanındaki CRUD (oluşturma, okuma, güncelleme, silme) işlemlerini yöneten arayüz.
    private final FlightRepository flightRepository;

    // Uçuş DTO nesneleri (Request/Response) ile Flight Entity nesnesi arasındaki dönüşümleri sağlayan mapper aracı.
    private final FlightMapper flightMapper;

    /**
     * Sisteme yeni bir uçuş kaydı ekler.
     *
     * @param flightRequest Kullanıcıdan veya istemciden gelen yeni uçuş bilgilerini içeren DTO.
     * @return Veritabanına kaydedilen uçuşun dışarıya aktarılacak yanıt formatındaki (DTO) hali.
     */
    @Override
    public FlightResponse addFlight(FlightRequest flightRequest) {

        // 1. Adım: İstemciden gelen DTO nesnesini veritabanı modeline (Entity) dönüştür.
        Flight flight = flightMapper.toEntity(flightRequest);

        // 2. Adım: Dönüştürülen entity'yi veritabanına kaydet (kaydedildikten sonra otomatik oluşturulan ID atanır).
        flight = flightRepository.save(flight);

        // 3. Adım: Kaydedilen güncel entity nesnesini tekrar istemcinin okuyabileceği Response DTO'ya çevir.
        FlightResponse flightResponse = flightMapper.toResponse(flight);

        // 4. Adım: DTO yanıtını geri dön.
        return flightResponse;
    }

    /**
     * Verilen benzersiz ID'ye göre belirli bir uçuşun detaylarını getirir.
     *
     * @param id Aranacak uçuşun birincil anahtar (Primary Key) değeri.
     * @return Bulunan uçuşun Response DTO formatındaki verisi.
     * @throws BaseException Uçuş bulunamazsa "error.flight_not_found" mesajıyla özel iş istisnası fırlatır.
     */
    @Override
    public FlightResponse getFlightById(Integer id) {

        // Veritabanında belirtilen ID'ye sahip uçuşu ara; bulamazsa hata fırlatarak akışı durdur.
        Flight flight = flightRepository.findById(id).orElseThrow(() -> new BaseException("error.flight_not_found"));

        // Bulunan uçuş entity nesnesini DTO'ya dönüştürerek döndür.
        return flightMapper.toResponse(flight);
    }

    /**
     * Sistemde kayıtlı olan tüm uçuşları liste halinde getirir.
     *
     * @return Tüm uçuşların FlightResponse DTO nesnelerinden oluşan listesi.
     */
    @Override
    public List<FlightResponse> getAllFlights() {

        // 1. Adım: Veritabanındaki tüm Flight kayıtlarını entity listesi olarak çek.
        List<Flight> flights = flightRepository.findAll();

        // 2. Adım: Dışarıya aktarılacak DTO nesnelerini toplamak için boş bir liste oluştur.
        List<FlightResponse> flightResponseList = new ArrayList<>();

        // 3. Adım: Veritabanından gelen her bir uçuş entity'sini tek tek gezerek DTO'ya dönüştür ve listeye ekle.
        for (Flight flight : flights) {
            FlightResponse flightResponse = flightMapper.toResponse(flight);
            flightResponseList.add(flightResponse);
        }

        // 4. Adım: DTO'larla doldurulmuş listeyi geri dön.
        return flightResponseList;
    }

    /**
     * Var olan bir uçuşun bilgilerini günceller.
     *
     * @param id Güncellenecek uçuşun ID değeri.
     * @param flightRequest Yeni uçuş bilgilerini taşıyan DTO.
     * @return Güncellenmiş uçuşun Response DTO hali.
     * @throws BaseException Belirtilen ID'ye ait uçuş yoksa hata fırlatır.
     */
    @Override
    public FlightResponse updateFlight(Integer id, FlightRequest flightRequest) {

        // 1. Adım: Güncellenmek istenen uçuş kaydını bul, kayıt yoksa hata fırlat.
        Flight flight = flightRepository.findById(id).orElseThrow(() -> new BaseException("error.flight_not_found"));

        // 2. Adım: String formatında gelen tarih/saat bilgisini ayrıştırmak için format kalıbını tanımla.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 3. Adım: Uçuş numarası, kalkış ve varış noktası gibi temel metin alanlarını güncelle.
        flight.setFlightNo(flightRequest.getFlightNo());
        flight.setDeparturePoint(flightRequest.getDeparturePoint());
        flight.setDestinationPoint(flightRequest.getDestinationPoint());

        // 4. Adım: Kalkış zamanı boş gelmediyse String metni LocalDateTime tipine dönüştürerek ata.
        if (flightRequest.getDepartureTime() != null) {
            flight.setDepartureTime(LocalDateTime.parse(flightRequest.getDepartureTime(), formatter));
        }

        // 5. Adım: Varış zamanı boş gelmediyse String metni LocalDateTime tipine dönüştürerek ata.
        if (flightRequest.getDestinationTime() != null) {
            flight.setDestinationTime(LocalDateTime.parse(flightRequest.getDestinationTime(), formatter));
        }

        // 6. Adım: Güncellenmiş entity nesnesini veritabanına yeniden kaydet.
        flight = flightRepository.save(flight);

        // 7. Adım: Son halini DTO'ya çevirip geri döndür.
        return flightMapper.toResponse(flight);
    }

    /**
     * Belirtilen ID'ye sahip uçuşu veritabanından tamamen siler.
     *
     * @param id Silinmek istenen uçuşun ID değeri.
     * @throws BaseException Silinecek uçuş bulunamazsa hata fırlatır.
     */
    @Override
    public void deleteFlight(Integer id) {

        // 1. Adım: Silinmek istenen uçuşun veritabanında var olup olmadığını doğrula.
        Flight flight = flightRepository.findById(id).orElseThrow(() -> new BaseException("error.flight_not_found"));

        // 2. Adım: Varlığı teyit edilen uçuşu repository aracılığıyla veritabanından kaldır.
        flightRepository.delete(flight);
    }
}