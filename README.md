# SkyRoute — Uçak Bileti Sistemi

Java 17, Spring Boot 3.2.2, PostgreSQL, Spring Security/JWT ve iyzico Sandbox kullanan backend.

## Çalıştırma

1. Java 17 ve PostgreSQL hazırlayın; boş bir `flightticketsystem` veritabanı oluşturun.
2. Çalıştırma ortamına aşağıdaki değişkenleri tanımlayın (IntelliJ Run Configuration → Environment variables kullanılabilir):

| Değişken | Açıklama |
|---|---|
| `DB_URL` | Varsayılan: `jdbc:postgresql://localhost:5432/flightticketsystem` |
| `DB_USERNAME` | Varsayılan: `postgres` |
| `DB_PASSWORD` | Veritabanı şifresi; zorunlu |
| `JWT_SECRET` | Base64 kodlanmış, en az 32 rastgele bayt içeren anahtar; zorunlu |
| `IYZICO_API_KEY` | Sandbox API anahtarı; zorunlu |
| `IYZICO_SECRET_KEY` | Sandbox gizli anahtarı; zorunlu |
| `IYZICO_BASE_URL` | Varsayılan: `https://sandbox-api.iyzipay.com` |

PowerShell'de JWT anahtarı oluşturma (çıktıyı yalnızca kendi ortamınıza kaydedin):

```powershell
$jwtBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
```

3. `./mvnw.cmd spring-boot:run` ile başlatın (macOS/Linux: `./mvnw spring-boot:run`).
4. Swagger: `http://localhost:8080/swagger-ui/index.html`.

Spring Boot bu projede `.env` dosyasını otomatik okumaz; ortam değişkenlerini IDE veya terminal üzerinden sağlayın. Anahtarları ve şifreleri repoya eklemeyin. Önceden paylaşılan anahtarlar yenilenmelidir; kaynak koddan kaldırmak Git geçmişini temizlemez.

## İlk yönetici

Admin/Customer rolleri başlangıçta hazırlanır. Yönetici hesabı varsayılan olarak oluşturulmaz.
İlk hesap için `BOOTSTRAP_ADMIN_ENABLED=true`, `BOOTSTRAP_ADMIN_EMAIL` ve en az 12 karakterlik `BOOTSTRAP_ADMIN_PASSWORD` tanımlayın. Oluşturduktan sonra bootstrap özelliğini kapatın. Var olan hesabın şifresi otomatik değiştirilmez; eski varsayılan hesap varsa ayrıca değiştirin.

## Erişim ve satış akışı

- Uçuş/koltuk görüntüleme, kayıt, giriş ve ödeme misafirlere açıktır.
- Koltuk ekleme/güncelleme/silme ve doğrudan ödemesiz bilet oluşturma yalnızca ADMIN içindir.
- Normal satış: `POST /api/v1/payments`; ödeme başarılı olunca bilet oluşturulur.
- `GET /api/v1/tickets/my-tickets` giriş yapan kullanıcının biletlerini getirir.
- Korumalı istekler `Authorization: Bearer <token>` başlığı taşır.

## Veritabanı bütünlüğü ve eşzamanlılık

Yeni şemalarda e-posta, PNR, biletin koltuğu ve uçuş içindeki koltuk numarası için benzersizlik kuralları vardır.
Mevcut PostgreSQL veritabanı için `docs/database-integrity.sql` dosyasını inceleyin. Önce yedek alın ve tekrar eden/eksik kayıtları kontrol edin. Script otomatik çalıştırılmaz; uygulama verilerine bu geliştirme sırasında müdahale edilmemiştir. Üretimde `ddl-auto=update` yerine incelenmiş migration ve `validate` kullanın.

Bilet oluşturma ve ödeme aynı koltuk satırını transaction içinde yazma kilidiyle alır. İkinci satış isteği bekler ve ilk satış tamamlanınca koltuğu dolu görür. Bilet kaydı ve koltuk güncellemesi birlikte commit/rollback edilir. Biletli koltuk güncellemesi engellenir.

Ödeme çağrısı sırasında kilit tutulduğu için yavaş sağlayıcı yanıtı beklemeyi uzatabilir. Transaction, dış servisteki tahsilatı geri alamaz: sağlayıcı başarılı olduktan sonra veritabanı hatası veya belirsiz ağ sonucu için kalıcı ödeme takibi, mutabakat ve iade akışı ayrıca tasarlanmalıdır. Bu çalışma otomatik iade eklemez.

## Hata yanıtları

`400`: geçersiz veri/JSON, `401`: doğrulanmamış kullanıcı, `403`: yetki yok, `404`: kayıt yok, `409`: çakışma/dolu koltuk/veritabanı bütünlük ihlali. Yanıtlar `status`, `message`, `timestamp` alanlarını taşır (güvenlik filtrelerinin yanıt biçimi farklı olabilir).

## Testler

```powershell
./mvnw.cmd test
```

Testler H2 ve taklit iyzico yanıtları kullanır; gerçek tahsilat yapılmaz. Eşzamanlı rezervasyon, rollback, benzersizlik, ödeme sonucu, erişim yetkileri, istek doğrulaması ve JWT yapılandırması kontrol edilir. H2 testleri PostgreSQL üzerinde yük testi yerine geçmez.
