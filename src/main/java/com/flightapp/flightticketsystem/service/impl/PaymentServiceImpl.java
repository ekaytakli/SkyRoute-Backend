package com.flightapp.flightticketsystem.service.impl;

import com.flightapp.flightticketsystem.dto.PaymentRequestDto;
import com.flightapp.flightticketsystem.dto.PaymentResponseDto;
import com.flightapp.flightticketsystem.entities.Flight;
import com.flightapp.flightticketsystem.entities.Seat;
import com.flightapp.flightticketsystem.entities.Ticket;
import com.flightapp.flightticketsystem.entities.User;
import com.flightapp.flightticketsystem.exception.BaseException;
import com.flightapp.flightticketsystem.repository.FlightRepository;
import com.flightapp.flightticketsystem.repository.SeatRepository;
import com.flightapp.flightticketsystem.repository.TicketRepository;
import com.flightapp.flightticketsystem.repository.UserRepository;
import com.flightapp.flightticketsystem.service.IPaymentService;

import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.Payment;
import com.iyzipay.model.PaymentCard;
import com.iyzipay.model.PaymentChannel;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.request.CreatePaymentRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * iyzico Sanal POS entegrasyonu üzerinden ödeme alma ve bilet oluşturma
 * süreçlerini yöneten servis sınıfıdır[cite: 1].
 *
 * Bu sınıf temel olarak şu adımları işletir:
 * 1. Uçuş ve koltuk geçerliliğini ve uygunluğunu doğrular.
 * 2. iyzico ödeme isteğini güvenli şekilde hazırlayıp API'ye gönderir.
 * 3. Ödeme başarılıysa bilet keser, PNR kodu üretir, koltuğu dolu yapar ve kullanıcıya bağlar.
 * 4. Ödeme başarısızsa hiçbir veritabanı değişikliği yapmadan hata mesajı döner.
 */
@Service // Spring IoC konteynerine bu sınıfın iş mantığı (business service) olduğunu tanıtır.
@RequiredArgsConstructor // 'final' tanımlı tüm alanlar için constructor tabanlı bağımlılık enjeksiyonunu (Constructor Injection) sağlar.
public class PaymentServiceImpl implements IPaymentService {

    // iyzico API anahtarları (apiKey, secretKey, baseUrl) konfigürasyonunu tutan nesne.
    private final Options iyzicoOptions;

    // Uçuş tablosu okuma/doğrulama işlemleri için kullanılan veri erişim katmanı.
    private final FlightRepository flightRepository;

    // Koltuğun durumu, fiyatı ve doluluk güncellemesi için kullanılan veri erişim katmanı.
    private final SeatRepository seatRepository;

    // Satış gerçekleştikten sonra veritabanına bilet kaydı atan arayüz[cite: 1].
    private final TicketRepository ticketRepository;

    // Eğer kullanıcı giriş yapmışsa bileti o kullanıcıya zimmetlemek için sorgulanan arayüz.
    private final UserRepository userRepository;

    /**
     * Ödeme işlemini başlatır ve sonucuna göre bilet oluşturur.
     *
     * @Transactional: Bu metot içerisindeki bilet kaydı ve koltuk güncelleme gibi tüm veritabanı
     * işlemlerini tek bir atomik işlem (transaction) altında toplar. Herhangi bir adımda hata olursa
     * yapılan tüm veritabanı değişiklikleri geri alınır (rollback edilir).
     *
     * @param requestDto Kullanıcıdan gelen kart bilgileri, kişisel bilgiler, uçuş ve koltuk ID'leri.
     * @return Başarı durumunda PNR kodu ve ödeme ID'sini, hata durumunda ise arıza mesajını içeren DTO nesnesi.
     */
    @Transactional
    @Override
    public PaymentResponseDto createPayment(PaymentRequestDto requestDto) {

        // =========================================================================================
        // 1. AŞAMA: ÖN KONTROLLER VE GÜVENLİK DOĞRULAMALARI (VALIDATION)
        // =========================================================================================

        // Frontend'den gelen flightId veritabanında var mı? Yoksa işlem devam edemez, hata fırlatılır.
        Flight flight = flightRepository.findById(requestDto.getFlightId())
                .orElseThrow(() -> new BaseException("error.flight_not_found"));

        // Satın alınmak istenen koltuk veritabanında mevcut mu kontrol edilir.
        Seat seat = seatRepository.findByIdForUpdate(requestDto.getSeatId())
                .orElseThrow(() -> new BaseException("error.seat_not_found"));

        // Güvenlik Kontrolü: Koltuk gerçekten seçilen uçuşa mı ait? (URL veya Request manipülasyonunu önler).
        if (!seat.getFlight().getId().equals(flight.getId())) {
            throw new BaseException("error.seat_not_belongs_to_flight");
        }

        // Koltuk zaten başka biri tarafından satın alınmış veya bloke edilmiş mi?
        if (!seat.isAvailable()) {
            throw new BaseException("error.seat_not_available");
        }

        // KRİTİK GÜVENLİK: İstemciden (frontend) gelen fiyat bilgisi ASLA kabul edilmez.
        // Fiyat doğrudan sunucu tarafındaki güvenilir veritabanı kaydından (seat.getPrice()) okunur.
        var amount = seat.getPrice();

        // =========================================================================================
        // 2. AŞAMA: IYZICO ÖDEME İSTEĞİ (PAYMENT REQUEST) HAZIRLIĞI
        // =========================================================================================

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();

        // İletişim dilini Türkçe olarak belirler.
        paymentRequest.setLocale(Locale.TR.getValue());

        // Her ödeme çağrısı için log takibini sağlayan tekil (unique) bir konuşma/işlem takip kodu üretilir.
        String conversationId = UUID.randomUUID().toString();
        paymentRequest.setConversationId(conversationId);

        // Tahsil edilecek ana tutar ve indirim sonrası ödenecek tutar veritabanı fiyatıyla eşitlenir.
        paymentRequest.setPrice(amount);
        paymentRequest.setPaidPrice(amount);

        // Para birimi Türk Lirası olarak belirlenir.
        paymentRequest.setCurrency(Currency.TRY.name());

        // Taksit sayısı: 1 (Tek çekim işlem yapılacağını gösterir).
        paymentRequest.setInstallment(1);

        // Sepet ID'si olarak uçağın uçuş numarası/ID'si referans verilir.
        paymentRequest.setBasketId("FLIGHT-" + flight.getId());

        // Ödeme kanalının bir web sitesi/tarayıcı olduğunu ifade eder.
        paymentRequest.setPaymentChannel(PaymentChannel.WEB.name());

        // Satışın türünün fiziksel/dijital ürün olduğunu belirtir.
        paymentRequest.setPaymentGroup(PaymentGroup.PRODUCT.name());

        // --- Kart Bilgilerinin Doldurulması ---
        // Kullanıcının formdan girdiği kart bilgileri iyzico modeline aktarılır (Bu bilgiler veritabanına asla yazılmaz).
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName(requestDto.getCardHolderName());
        paymentCard.setCardNumber(requestDto.getCardNumber());
        paymentCard.setExpireMonth(requestDto.getExpireMonth());
        paymentCard.setExpireYear(requestDto.getExpireYear());
        paymentCard.setCvc(requestDto.getCvc());
        // 0: Kartı iyzico sisteminde saklama (kart saklama özelliği kapalı).
        paymentCard.setRegisterCard(0);

        paymentRequest.setPaymentCard(paymentCard);

        // --- Alıcı (Buyer / Müşteri) Bilgilerinin Hazırlanması ---
        Buyer buyer = new Buyer();
        // Sandbox testi için rastgele tekil bir müşteri ID'si atanır.
        buyer.setId("USER-" + UUID.randomUUID());
        // İsim, soyisim ve e-posta formdan gelmişse alınır; boşsa test varsayılanları atanır.
        buyer.setName(requestDto.getFirstName() != null ? requestDto.getFirstName() : "Test");
        buyer.setSurname(requestDto.getLastName() != null ? requestDto.getLastName() : "User");
        buyer.setEmail(requestDto.getEmail() != null ? requestDto.getEmail() : "test@example.com");
        // Sandbox gereksinimi olan kimlik ve adres bilgileri test değerleriyle doldurulur.
        buyer.setIdentityNumber("11111111111");
        buyer.setRegistrationAddress("Sandbox Test Address");
        buyer.setIp("127.0.0.1");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setZipCode("34000");

        paymentRequest.setBuyer(buyer);

        // --- Fatura Adresi (Billing Address) Hazırlanması ---
        Address billingAddress = new Address();
        billingAddress.setContactName(buyer.getName() + " " + buyer.getSurname());
        billingAddress.setCity("Istanbul");
        billingAddress.setCountry("Turkey");
        billingAddress.setAddress("Sandbox Test Address");
        billingAddress.setZipCode("34000");

        paymentRequest.setBillingAddress(billingAddress);

        // --- Sepet Kalemleri (Basket Item) Hazırlanması ---
        // Alınan bilet bir sanal ürün (Virtual) olarak iyzico sepetine eklenir.
        BasketItem basketItem = new BasketItem();
        basketItem.setId("SEAT-" + seat.getId());
        basketItem.setName("Flight " + flight.getId() + " Seat " + seat.getSeatNumber());
        basketItem.setCategory1("Flight Ticket");
        basketItem.setItemType(BasketItemType.VIRTUAL.name());
        basketItem.setPrice(amount);

        // Tek bir koltuk alındığı için tekil liste halinde sepete verilir.
        paymentRequest.setBasketItems(Collections.singletonList(basketItem));

        // =========================================================================================
        // 3. AŞAMA: IYZICO İLE HABERLEŞME (API CALL)
        // =========================================================================================

        // Hazırlanan ödeme paketi ve yetki anahtarları ile iyzico API'sine istek atılır.
        Payment payment = Payment.create(paymentRequest, iyzicoOptions);

        // =========================================================================================
        // 4. AŞAMA: İŞLEM SONUCUNUN DEĞERLENDİRİLMESİ
        // =========================================================================================

        // iyzico API'sinden dönen yanıt başarılı ("success") mı?
        if ("success".equalsIgnoreCase(payment.getStatus())) {

            // 1. Yeni Bilet Entity'si oluşturulur.
            Ticket ticket = new Ticket();

            // Havacılık standardına uygun rastgele 6 haneli büyük harfli benzersiz bir PNR kodu üretilir.
            String pnrKodu = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
            ticket.setPnrKodu(pnrKodu);

            // Biletin kesilme anı sistem zamanı olarak atanır.
            ticket.setDate(LocalDateTime.now());

            // Bilet ilgili uçuş ve seçilen koltuk ile ilişkilendirilir.
            ticket.setFlight(flight);
            ticket.setSeat(seat);

            // 2. Güvenlik Bağlamından (Security Context) oturum açmış kullanıcı kontrol edilir.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kullanıcı anonim değilse ve sisteme giriş yapmışsa bilet doğrudan o kullanıcıya zimmetlenir.
            if (authentication != null &&
                    authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal())) {

                // Giriş yapan kullanıcının kimlik bilgisi (e-posta) alınır.
                String email = authentication.getName();

                // Kullanıcı veritabanından çekilir.
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new BaseException("error.user_not_found"));

                // Bilet kullanıcının hesabına bağlanır.
                ticket.setUser(user);
            }
            // (Eğer kullanıcı giriş yapmadan 'misafir' olarak bilet almışsa ticket.user alanı null kalır)

            // 3. Bilet veritabanına kalıcı olarak kaydedilir.
            Ticket savedTicket = ticketRepository.save(ticket);

            // 4. Satın alınan koltuk başkası tarafından alınamasın diye 'müsait değil' (false) yapılır.
            seat.setAvailable(false);
            seatRepository.save(seat);

            // 5. Başarılı sonuç, üretilen gerçek PNR kodu ve iyzico ödeme referans numarasıyla frontend'e dönülür.
            return PaymentResponseDto.builder()
                    .status("SUCCESS")
                    .paymentId(payment.getPaymentId())
                    .pnrKodu(savedTicket.getPnrKodu())
                    .message("Ödeme ve bilet işlemi başarıyla tamamlandı.")
                    .build();
        }

        // =========================================================================================
        // 5. AŞAMA: ÖDEME BAŞARISIZ İSE GERİ DÖNÜŞ
        // =========================================================================================

        // iyzico ödemeyi onaylamadıysa veritabanında hiçbir işlem yapılmaz; hata mesajı DTO içinde dönülür.
        return PaymentResponseDto.builder()
                .status("FAILED")
                .message(payment.getErrorMessage() != null ? payment.getErrorMessage() : "Ödeme işlemi başarısız oldu.")
                .build();
    }
}