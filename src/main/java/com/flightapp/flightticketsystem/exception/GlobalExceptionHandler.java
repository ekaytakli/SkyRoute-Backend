package com.flightapp.flightticketsystem.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Uygulamanın tüm Controller sınıflarında fırlatılan hataları tek bir merkezden yakalayan,
 * çoklu dil desteğine göre çeviren ve standart ErrorResponse formatında dönen global hata yöneticisidir.
 */
@RestControllerAdvice // Spring'e bu sınıfın tüm @RestController'lar için global bir istisna yakalayıcı (AOP tavsiyesi) olduğunu bildirir.
@RequiredArgsConstructor // 'final' tanımlı MessageSource bağımlılığı için yapıcı metot (Constructor Injection) üretir.
public class GlobalExceptionHandler {

    // messages.properties, messages_tr.properties vb. kaynak dosyalarından metin okuyan Spring bileşeni
    private final MessageSource messageSource;

    /**
     * İş mantığında (Service katmanında) bilerek fırlattığımız özel 'BaseException' hatalarını yakalar.
     * (Örn: Uçuş bulunamadı, koltuk dolu vb.)
     *
     * @param ex Yakalanan BaseException nesnesi.
     * @return 400 Bad Request HTTP kodu ve tercüme edilmiş hata mesajı içeren ErrorResponse.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        // 1. Adım: Hata içindeki anahtarı (messageKey) ve varsa argümanları kullanıcının diline göre çevir.
        String message = getMessage(ex.getMessageKey(), ex.getArgs());

        // 2. Adım: Standart ErrorResponse modelini 400 kodu ve çevrilmiş mesajla oluştur.
        ErrorResponse response = new ErrorResponse(ex.getStatus().value(), message);

        // 3. Adım: İstemciye 400 HTTP durum koduyla JSON olarak dön.
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleConflict(Exception ex) {
        return error(HttpStatus.CONFLICT, "error.data_conflict");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "error.forbidden");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "error.invalid_request");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String key) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), getMessage(key, null)));
    }

    /**
     * Kullanıcı login olurken yanlış şifre veya e-posta girdiğinde Spring Security'nin
     * otomatik olarak fırlattığı BadCredentialsException hatasını yakalar.
     *
     * @param ex Yakalanan kimlik doğrulama hatası.
     * @return 401 Unauthorized HTTP kodu ve güvenlik hata mesajı.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        // Dil dosyasındaki 'error.bad_credentials' anahtarının karşılığını çek.
        String message = getMessage("error.bad_credentials", null);

        // 401 Unauthorized durum koduyla standart hata cevabını hazırla.
        ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), message);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Kodda öngörülemeyen veya yukarıdaki özel bloklara girmeyen beklenmedik
     * tüm genel sistem hatalarını (NullPointerException, SQL hataları vb.) yakalar.
     *
     * @param ex Yakalanan genel Exception nesnesi.
     * @return 500 Internal Server Error HTTP kodu ve genel sunucu hata mesajı.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Kullanıcıya karmaşık sistem/kod hatalarını göstermek yerine genel bir sunucu hatası mesajı çek.
        String message = getMessage("error.internal_server", null);

        // 500 Internal Server Error kodlu yanıtı oluştur.
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DTO'lar üzerindeki validasyon anotasyonları (@NotBlank, @NotNull vb.)
     * kuralları ihlal edildiğinde fırlatılan MethodArgumentNotValidException hatasını yakalar.
     *
     * @param ex Doğrulama hatası nesnesi.
     * @return 400 Bad Request HTTP kodu ve alan doğrulama hata açıklaması.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 1. Adım: Oluşan ilk doğrulama hatasının varsayılan mesajını al (Örn: "{error.flight_no_blank}")
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        // 2. Adım: Eğer mesaj {anahtar} formatındaysa parantezleri temizle ve dil dosyasından çevirisini bul
        if (errorMessage != null && errorMessage.startsWith("{") && errorMessage.endsWith("}")) {
            String key = errorMessage.substring(1, errorMessage.length() - 1);
            errorMessage = getMessage(key, null);
        }

        // 3. Adım: Çözümlenen validasyon mesajıyla 400 Bad Request cevabı dön.
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Verilen hata anahtarını (key), kullanıcının o anki geçerli dil ayarını (Locale)
     * otomatik tespit ederek MessageSource üzerinden ilgili dildeki metne dönüştüren yardımcı metot.
     *
     * @param key messages.properties dosyasındaki metin anahtarı.
     * @param args Metin içindeki yer tutuculara ({0}, {1}) yerleştirilecek dinamik değerler.
     * @return Çevrilmiş metin (eğer anahtar bulunamazsa anahtarın kendi adı döner).
     */
    private String getMessage(String key, Object[] args) {
        try {
            // LocaleContextHolder.getLocale() ile gelen HTTP isteğindeki Accept-Language başlığı okunur.
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            // Dil dosyasında anahtar karşılığı yoksa uygulamanın çökmemesi için anahtarın kendisini döner.
            return key;
        }
    }
}
