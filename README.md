# ✈️ SkyRoute — Uçak Bileti Rezervasyon Sistemi (Backend)

SkyRoute, modern bir uçak bileti arama, koltuk seçimi, rezervasyon ve ödeme süreçlerini yöneten Spring Boot tabanlı RESTful backend servisidir.

## 🛠️ Teknolojiler
- **Java 17 & Spring Boot** (Web, Data JPA, Security)
- **PostgreSQL**
- **JWT (JSON Web Token)** ile Kimlik Doğrulama & Yetkilendirme
- **İyzico** Sanal POS Ödeme Entegrasyonu
- **Rate Limiting** (İstek Sınırlandırma)
- **i18n** (Çoklu Dil Desteği - TR/EN)
- **Maven**

## 🌟 Özellikler
- **Kullanıcı & Rol Yönetimi:** JWT tabanlı güvenli kayıt ve giriş (User, Admin).
- **Uçuş & Koltuk İşlemleri:** Uçuş rotaları listeleme, anlık koltuk müsaitlik takibi.
- **Bilet & Rezervasyon:** Güvenli koltuk ayırtma ve bilet oluşturma.
- **Ödeme Sistemi:** İyzico entegrasyonu ile ödeme adımları.
- **Güvenlik & Hız:** Interceptor tabanlı API hız sınırlaması (Rate Limiting).
- **Merkezi Hata Yönetimi:** Standart hata formatı (`ErrorResponse`).

## 🚀 Kurulum & Çalıştırma

1. Projeyi klonlayın:
```bash
git clone [https://github.com/ekaytakli/SkyRoute-Backend.git](https://github.com/ekaytakli/SkyRoute-Backend.git)
