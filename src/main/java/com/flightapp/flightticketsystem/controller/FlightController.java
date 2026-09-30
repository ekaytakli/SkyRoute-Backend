package com.flightapp.flightticketsystem.controller;

import com.flightapp.flightticketsystem.dto.FlightRequest;
import com.flightapp.flightticketsystem.dto.FlightResponse;
import com.flightapp.flightticketsystem.service.IFlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Uçuş ekleme, listeleme, güncelleme ve silme HTTP isteklerini yöneten REST Controller bileşenidir[cite: 1].
 */
@RestController // Bu sınıfın REST API uç noktaları sunduğunu ve metot dönüşlerinin JSON olarak serileştirileceğini bildirir[cite: 1].
@RequestMapping(path = "/rest/api/flight") // Bu sınıftaki tüm uç noktaların kök URL ön ekini (/rest/api/flight) belirler[cite: 1].
@RequiredArgsConstructor // 'final' tanımlı bağımlılıkları (IFlightService) otomatik olarak Constructor Injection ile bağlar[cite: 1].
public class FlightController {

    // Uçuş iş mantıklarını (business logic) yürüten servis arayüzü[cite: 1]
    private final IFlightService flightService;

    /**
     * Sisteme yeni bir uçuş ekler[cite: 1].
     * Rota: POST /rest/api/flight/add-flight[cite: 1]
     *
     * @PreAuthorize: Yalnızca 'ADMIN' rolüne sahip olan yetkili kullanıcıların erişimine izin verir[cite: 1].
     * @param flightRequest JSON gövdesinden gelen uçuş verileri[cite: 1]. @Valid ile zorunlu alan kontrolleri yapılır[cite: 1].
     * @return Eklenen uçuşun Response DTO formatındaki özeti[cite: 1].
     */
    @PreAuthorize("hasRole('ADMIN')") // Bu metoda sadece yetkilerinde ROLE_ADMIN olan kullanıcılar girebilir[cite: 1]
    @PostMapping(path = "/add-flight")
    public FlightResponse addFlight(@Valid @RequestBody FlightRequest flightRequest) {
        // İsteği doğrudan uçuş servisine iletir ve oluşan yeni uçuşu döner[cite: 1].
        return flightService.addFlight(flightRequest);
    }

    /**
     * Sistemde tanımlı tüm aktif uçuşları listeler[cite: 1].
     * Rota: GET /rest/api/flight/list-all-flights[cite: 1]
     *
     * Bu uç nokta SecurityConfig'de GET izinlerinde olduğu için oturum açmamış misafir kullanıcılar tarafından da çağrılabilir[cite: 1].
     *
     * @return Sistemdeki tüm uçuşların FlightResponse listesi[cite: 1].
     */
    @GetMapping(path = "/list-all-flights")
    public List<FlightResponse> listAllFlights() {
        // Servis katmanından tüm uçuşları çekip istemciye döner[cite: 1].
        return flightService.getAllFlights();
    }

    /**
     * ID numarası verilen belirli bir uçuşun detay bilgilerini getirir[cite: 1].
     * Rota: GET /rest/api/flight/flights-{id}[cite: 1]
     *
     * @param id URL yolu üzerinden gönderilen uçuşun benzersiz kimlik numarası (örn: /flights-3)[cite: 1].
     * @return İlgili uçuşun bilgileri[cite: 1].
     */
    @GetMapping(path = "/flights-{id}")
    public FlightResponse getFlightById(@PathVariable(name = "id") Integer id){
        // Verilen ID'ye sahip uçuşu servis katmanından sorgular[cite: 1].
        return flightService.getFlightById(id);
    }

    /**
     * Var olan bir uçuşun bilgilerini (sefer saati, kalkış/varış yeri vb.) günceller[cite: 1].
     * Rota: PUT /rest/api/flight/save-flight-{id}[cite: 1]
     *
     * @PreAuthorize: Uçuş bilgilerini sadece 'ADMIN' rolündeki yöneticiler güncelleyebilir[cite: 1].
     * @param id Güncellenecek uçuşun kimlik numarası[cite: 1].
     * @param flightRequest Yeni uçuş parametrelerini taşıyan DTO[cite: 1].
     * @return Güncellenmiş uçuşun Response DTO hali[cite: 1].
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/save-flight-{id}")
    public FlightResponse saveFlight(@PathVariable(name = "id") Integer id , @Valid @RequestBody FlightRequest flightRequest){
        // Güncelleme isteğini ve uçuş ID'sini servis katmanına devreder[cite: 1].
        return flightService.updateFlight(id, flightRequest);
    }

    /**
     * Belirtilen ID'ye sahip uçuşu sistemden tamamen siler[cite: 1].
     * Rota: DELETE /rest/api/flight/delete-flight-{id}[cite: 1]
     *
     * @PreAuthorize: Sefer silme yetkisi yalnızca 'ADMIN' rolüne aittir[cite: 1].
     * @param id Silinecek uçuşun kimlik numarası[cite: 1].
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/delete-flight-{id}")
    public void deleteFlight(@PathVariable(name = "id") Integer id){
        // Servis katmanına uçuşu silmesi talimatını verir[cite: 1].
        flightService.deleteFlight(id);
    }
}